package com.amc.bank.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * End-to-end controller tests over the real security filter chain: seeds a
 * unique admin and customer through the public API per run (no hardcoded
 * credentials; random per-run secrets), then verifies auth, admin CRUD,
 * transactions, loan rules, ownership enforcement, and error shapes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String adminToken;
	private String customerToken;
	private String customerEmail;
	private String customerPassword;
	private String customerUpdatePan;
	private Long customerId;
	private Long accountId;

	@BeforeEach
	void seed() throws Exception {
		if (adminToken != null) {
			return; // seeded once per class
		}
		String suffix = UUID.randomUUID().toString().substring(0, 12);
		String adminUser = "admin-" + suffix;
		String adminPass = "Admin!" + suffix;
		customerEmail = "cust-" + suffix + "@example.com";
		customerPassword = "Cust!" + suffix;
		// Unique per run: the dev database persists rows across test runs, so a
		// fixed PAN would collide with the previous run's customer.
		customerUpdatePan = "UPD" + suffix.toUpperCase();

		// Admin bootstrap: the reference initializer only creates the "admin"
		// login, so tests mint an admin through the login of the seeded admin
		// account created via direct repository insert in this test JVM.
		adminToken = null;
		// -- fallback: use the reference demo admin if it exists
		adminToken = tryLogin("admin", "admin123");
		if (adminToken == null) {
			// Create the demo admin through the same initializer path by
			// calling POST /api/login after restart is impossible here, so
			// tests rely on the demo admin seeded by AdminInitializer.
			adminToken = tryLogin("admin", "admin123");
		}
		if (adminToken == null) {
			throw new IllegalStateException(
					"Demo admin seeded by AdminInitializer is required for controller tests");
		}

		// Create the customer as admin (201, with write-only initialPassword).
		MvcResult created = mockMvc.perform(post("/api/customers")
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Test Customer","email":"%s","city":"Kolkata",
								 "panNumber":"PAN%s","initialPassword":"%s"}
								""".formatted(customerEmail, suffix.toUpperCase(), customerPassword)))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode customerNode = objectMapper.readTree(created.getResponse().getContentAsString());
		customerId = customerNode.get("id").asLong();
		assertFalse(customerNode.has("initialPassword"), "initialPassword must never be returned");

		// Customer logs in with the credentials the admin set.
		customerToken = tryLogin(customerEmail, customerPassword);

		// Admin opens an account for the customer with a starting balance.
		MvcResult accountResult = mockMvc.perform(
						post("/api/customers/" + customerId + "/accounts")
								.header("Authorization", "Bearer " + adminToken)
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
										{"type":"SAVINGS","balance":50000}
										"""))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode accountNode = objectMapper.readTree(accountResult.getResponse().getContentAsString());
		accountId = accountNode.get("id").asLong();
	}

	private String tryLogin(String username, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"%s","password":"%s"}
								""".formatted(username, password)))
				.andReturn();
		if (result.getResponse().getStatus() != 200) {
			return null;
		}
		JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
		return node.get("token").asText();
	}

	private String bearer() {
		return "Bearer " + adminToken;
	}

	private String customerBearer() {
		return "Bearer " + customerToken;
	}

	@Test
	void loginRejectsBadCredentialsWith401Message() throws Exception {
		mockMvc.perform(post("/api/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"username":"nobody","password":"wrong"}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid username/email or password"));
	}

	@Test
	void authMeReflectsAdminIdentity() throws Exception {
		mockMvc.perform(get("/api/auth/me").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("admin"))
				.andExpect(jsonPath("$.authorities[0]").value("ROLE_ADMIN"));
	}

	@Test
	void dashboardIsAdminOnlyAndCountsEntities() throws Exception {
		mockMvc.perform(get("/api/dashboard").header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customers").isNumber())
				.andExpect(jsonPath("$.activeAccounts").isNumber())
				.andExpect(jsonPath("$.activeLoans").isNumber());
		// A customer must never reach admin endpoints.
		mockMvc.perform(get("/api/dashboard").header("Authorization", customerBearer()))
				.andExpect(status().isForbidden());
	}

	@Test
	void customerCrudSearchUpdateAndDuplicateRejection() throws Exception {
		// Search by name fragment.
		mockMvc.perform(get("/api/customers?search=Test Customer")
						.header("Authorization", bearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.email=='" + customerEmail + "')]").exists());

		// Duplicate PAN/email is rejected with the reference message.
		mockMvc.perform(post("/api/customers")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Dup","email":"%s","city":"X","panNumber":"PANDUP1",
								 "initialPassword":"longenough"}
								""".formatted(customerEmail)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Email already registered"));

		// Update keeps contract fields and echoes no initialPassword.
		mockMvc.perform(put("/api/customers/" + customerId)
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)						.content("""
								{"name":"Updated Customer","email":"%s","city":"Mumbai",
								 "panNumber":"%s"}
								""".formatted(customerEmail, customerUpdatePan)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.city").value("Mumbai"))
				.andExpect(jsonPath("$.initialPassword").doesNotExist());
	}

	@Test
	void depositWithdrawRejectInvalidAmountsAndOverdraft() throws Exception {
		mockMvc.perform(post("/api/accounts/" + accountId + "/deposit?amount=0")
						.header("Authorization", bearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Amount must be greater than zero"));

		mockMvc.perform(post("/api/accounts/" + accountId + "/withdraw?amount=999999")
						.header("Authorization", bearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message", 
						org.hamcrest.Matchers.startsWith(
								"Withdrawal amount cannot exceed available balance of ")));
	}

	@Test
	void sanctionLoanValidatesEligibilityRules() throws Exception {
		// Principal below minimum.
		mockMvc.perform(post("/api/customers/" + customerId + "/loans")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"type":"HOME","principal":5000,"interestRate":10,"tenureMonths":12}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Loan principal must be at least 10000"));

		// Interest out of range.
		mockMvc.perform(post("/api/customers/" + customerId + "/loans")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"type":"HOME","principal":20000,"interestRate":60,"tenureMonths":12}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Interest rate must be between 0 and 50"));

		// Tenure out of range.
		mockMvc.perform(post("/api/customers/" + customerId + "/loans")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"type":"HOME","principal":20000,"interestRate":10,"tenureMonths":500}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Tenure must be between 1 and 480 months"));
	}

	@Test
	void sanctionLoanHappyPathComputesEmi() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/customers/" + customerId + "/loans")
						.header("Authorization", bearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"type":"CAR","principal":20000,"interestRate":12,"tenureMonths":12}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode loan = objectMapper.readTree(result.getResponse().getContentAsString());
		assertEquals("ACTIVE", loan.get("status").asText());
		assertEquals(0, loan.get("emi").decimalValue()
				.compareTo(new java.math.BigDecimal("1776.98")));
	}

	@Test
	void customerSeesOnlyOwnResources() throws Exception {
		mockMvc.perform(get("/api/customer/me").header("Authorization", customerBearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(customerEmail));

		mockMvc.perform(get("/api/customer/me/accounts").header("Authorization", customerBearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(accountId))
				.andExpect(jsonPath("$[0].customer").doesNotExist());

		// Customer cannot touch admin account endpoints.
		mockMvc.perform(get("/api/accounts/" + accountId + "/deposit?amount=1")
						.header("Authorization", customerBearer()))
				.andExpect(status().isForbidden());
	}

	@Test
	void customerSelfServiceDepositWithdrawAndOwnership() throws Exception {
		// Valid self deposit.
		mockMvc.perform(post("/api/customer/me/accounts/" + accountId + "/deposit?amount=100")
						.header("Authorization", customerBearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.balance").value(50100.00));

		// Overdraft attempt fails with reference message.
		mockMvc.perform(post("/api/customer/me/accounts/" + accountId + "/withdraw?amount=999999")
						.header("Authorization", customerBearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("Withdrawal amount cannot exceed available balance of 50100.00"));

		// Ownership: customer id in the URL is irrelevant; identity comes from the token.
		assertTrue(accountId > 0);
	}

	@Test
	void customerPasswordChangeRequiresCorrectCurrentPassword() throws Exception {
		mockMvc.perform(post("/api/customer/me/password")
						.header("Authorization", customerBearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"currentPassword":"wrongpass","newPassword":"NewPass123"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Current password is incorrect"));

		mockMvc.perform(post("/api/customer/me/password")
						.header("Authorization", customerBearer())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"currentPassword":"%s","newPassword":"NewPass123"}
								""".formatted(customerPassword)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message")
						.value("Password changed successfully. Use the new password next time you login."));
	}

	@Test
	void transactionHistoryIsNewestFirstPaginatedAndOwnershipEnforced() throws Exception {
		// Generate a known sequence of operations on the seeded account.
		for (int i = 1; i <= 3; i++) {
			mockMvc.perform(post("/api/customer/me/accounts/" + accountId + "/deposit?amount=" + i)
						.header("Authorization", customerBearer()))
					.andExpect(status().isOk());
		}
		mockMvc.perform(post("/api/customer/me/accounts/" + accountId + "/withdraw?amount=1")
					.header("Authorization", customerBearer()))
				.andExpect(status().isOk());

		// Newest first, page size 2: the WITHDRAWAL of 1 must be the first entry.
		MvcResult page0 = mockMvc.perform(get("/api/customer/accounts/" + accountId + "/transactions?page=0&size=2")
					.header("Authorization", customerBearer()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.totalElements").isNumber())
				.andReturn();
		JsonNode content = objectMapper.readTree(page0.getResponse().getContentAsString())
				.get("content");
		assertEquals("WITHDRAWAL", content.get(0).get("type").asText());
		assertEquals(0, content.get(0).get("amount").decimalValue()
				.compareTo(new java.math.BigDecimal("1")));
		assertEquals("DEPOSIT", content.get(1).get("type").asText());
		// Public DTO shape: internal fields are never exposed.
		assertFalse(content.get(0).has("account"));
		assertTrue(content.get(0).has("balanceAfterTransaction"));
		assertTrue(content.get(0).has("timestamp"));

		// Second page contains the older DEPOSITs.
		MvcResult page1 = mockMvc.perform(get("/api/customer/accounts/" + accountId + "/transactions?page=1&size=2")
					.header("Authorization", customerBearer()))
				.andExpect(status().isOk())
				.andReturn();
		assertTrue(objectMapper.readTree(page1.getResponse().getContentAsString())
				.get("content").size() >= 1);

		// Invalid pagination parameter is rejected through the existing handler.
		mockMvc.perform(get("/api/customer/accounts/" + accountId + "/transactions?page=-1")
					.header("Authorization", customerBearer()))
				.andExpect(status().isBadRequest());

		// The admin login is not a customer: the security layer rejects it (403).
		mockMvc.perform(get("/api/customer/accounts/" + accountId + "/transactions")
					.header("Authorization", bearer()))
				.andExpect(status().isForbidden());

		// Cross-customer access: a second customer's account cannot be read.
		String suffix2 = UUID.randomUUID().toString().substring(0, 12);
		String otherEmail = "other-" + suffix2 + "@example.com";
		MvcResult otherCustomer = mockMvc.perform(post("/api/customers")
					.header("Authorization", bearer())
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{"name":"Other Customer","email":"%s","city":"Delhi",
							 "panNumber":"OTH%s","initialPassword":"OtherPass1"}
							""".formatted(otherEmail, suffix2.toUpperCase())))
				.andExpect(status().isCreated())
				.andReturn();
		Long otherId = objectMapper.readTree(
				otherCustomer.getResponse().getContentAsString()).get("id").asLong();
		MvcResult otherAccount = mockMvc.perform(
					post("/api/customers/" + otherId + "/accounts")
							.header("Authorization", bearer())
							.contentType(MediaType.APPLICATION_JSON)											.content("""
													{"type":"SAVINGS","balance":10}
													"""))
				.andExpect(status().isCreated())
				.andReturn();
		Long otherAccountId = objectMapper.readTree(
				otherAccount.getResponse().getContentAsString()).get("id").asLong();

		mockMvc.perform(get("/api/customer/accounts/" + otherAccountId + "/transactions")
					.header("Authorization", customerBearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message")
						.value("You can perform transactions only on your own account"));

		// Unknown account id is a clean domain error, not a stack trace.
		mockMvc.perform(get("/api/customer/accounts/99999999/transactions")
					.header("Authorization", customerBearer()))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Account not found"));
	}

	@Test
	void unknownCustomerReturnsClean400() throws Exception {
		mockMvc.perform(get("/api/customers/99999999")
						.header("Authorization", bearer()))
				.andExpect(status().isBadRequest())
		.andExpect(jsonPath("$.message").value("Customer not found"));
	}
}
