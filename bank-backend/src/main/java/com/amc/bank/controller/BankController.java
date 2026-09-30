package com.amc.bank.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amc.bank.dto.AccountRequest;
import com.amc.bank.dto.CustomerRequest;
import com.amc.bank.dto.DashboardResponse;
import com.amc.bank.dto.LoanRequest;
import com.amc.bank.dto.MessageResponse;
import com.amc.bank.dto.PasswordChangeRequest;
import com.amc.bank.dto.TransactionResponse;
import com.amc.bank.model.Account;
import com.amc.bank.model.Customer;
import com.amc.bank.model.Loan;
import com.amc.bank.service.BankService;

/**
 * Admin and customer-self-service endpoints, mirroring the reference API
 * layout. Controllers stay thin: business rules live in BankService, and
 * customer identity is always derived from the authenticated principal, never
 * from URL-supplied ids.
 */
@RestController
@RequestMapping("/api")
public class BankController {

	private final BankService service;

	public BankController(BankService service) {
		this.service = service;
	}

	// -------------------- ADMIN APIs --------------------

	@GetMapping("/dashboard")
	public DashboardResponse dashboard() {
		return new DashboardResponse(
				service.customerCount(),
				service.activeAccountCount(),
				service.activeLoanCount());
	}

	@GetMapping("/customers")
	public List<Customer> customers(@RequestParam(defaultValue = "") String search) {
		return service.getCustomers(search);
	}

	@GetMapping("/customers/{id}")
	public Customer customer(@PathVariable Long id) {
		return service.getCustomer(id);
	}

	// Customer creation JSON includes initialPassword (write-only). It is never
	// stored on the entity and never returned in responses.
	@PostMapping("/customers")
	public ResponseEntity<Customer> addCustomer(@RequestBody CustomerRequest request) {
		Customer saved = service.addCustomer(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@PutMapping("/customers/{id}")
	public Customer updateCustomer(@PathVariable Long id,
			@RequestBody CustomerRequest request) {
		return service.updateCustomer(id, request);
	}

	@DeleteMapping("/customers/{id}")
	public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
		service.deleteCustomer(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/customers/{customerId}/accounts")
	public List<Account> accounts(@PathVariable Long customerId) {
		return service.getAccounts(customerId);
	}

	@PostMapping("/customers/{customerId}/accounts")
	public ResponseEntity<Account> openAccount(@PathVariable Long customerId,
			@RequestBody AccountRequest request) {
		Account account = service.openAccount(customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(account);
	}

	@PostMapping("/accounts/{accountId}/deposit")
	public Account deposit(@PathVariable Long accountId,
			@RequestParam BigDecimal amount) {
		return service.deposit(accountId, amount);
	}

	@PostMapping("/accounts/{accountId}/withdraw")
	public Account withdraw(@PathVariable Long accountId,
			@RequestParam BigDecimal amount) {
		return service.withdraw(accountId, amount);
	}

	@GetMapping("/loans")
	public List<Loan> allLoans() {
		return service.getAllLoans();
	}

	@GetMapping("/customers/{customerId}/loans")
	public List<Loan> loans(@PathVariable Long customerId) {
		return service.getLoans(customerId);
	}

	@PostMapping("/customers/{customerId}/loans")
	public ResponseEntity<Loan> sanctionLoan(@PathVariable Long customerId,
			@RequestBody LoanRequest request) {
		Loan loan = service.sanctionLoan(customerId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(loan);
	}

	// -------------------- CUSTOMER SELF-SERVICE APIs --------------------

	@GetMapping("/customer/me")
	public Customer myProfile(Authentication authentication) {
		return service.getCustomerForUsername(authentication.getName());
	}

	@GetMapping("/customer/me/accounts")
	public List<Account> myAccounts(Authentication authentication) {
		return service.getMyAccounts(authentication.getName());
	}

	@GetMapping("/customer/me/loans")
	public List<Loan> myLoans(Authentication authentication) {
		return service.getMyLoans(authentication.getName());
	}

	// Customer can deposit only into an account linked to their own login.
	@PostMapping("/customer/me/accounts/{accountId}/deposit")
	public Account depositMyAccount(Authentication authentication,
			@PathVariable Long accountId,
			@RequestParam BigDecimal amount) {
		return service.depositMyAccount(authentication.getName(), accountId, amount);
	}

	// Customer can withdraw only from their own account and only when
	// amount <= available balance; BankService performs the ownership/balance checks.
	@PostMapping("/customer/me/accounts/{accountId}/withdraw")
	public Account withdrawMyAccount(Authentication authentication,
			@PathVariable Long accountId,
			@RequestParam BigDecimal amount) {
		return service.withdrawMyAccount(authentication.getName(), accountId, amount);
	}

	/**
	 * Transaction history for one of the logged-in customer's own accounts,
	 * newest first. Ownership is enforced in BankService against the
	 * authenticated identity, never against the URL id alone.
	 */
	@GetMapping("/customer/accounts/{accountId}/transactions")
	public Page<TransactionResponse> transactionHistory(Authentication authentication,
			@PathVariable Long accountId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(
				page,
				Math.min(Math.max(size, 1), 100),
				Sort.by(Sort.Direction.DESC, "timestamp"));
		return service.getTransactionHistory(authentication.getName(), accountId, pageable)
				.map(TransactionResponse::from);
	}

	@PostMapping("/customer/me/password")
	public ResponseEntity<MessageResponse> changeMyPassword(
			Authentication authentication,
			@RequestBody PasswordChangeRequest request) {
		service.changeCustomerPassword(
				authentication.getName(),
				request.getCurrentPassword(),
				request.getNewPassword());
		return ResponseEntity.ok(new MessageResponse(
				"Password changed successfully. Use the new password next time you login."));
	}
}
