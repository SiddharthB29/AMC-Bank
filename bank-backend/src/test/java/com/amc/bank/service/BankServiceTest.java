package com.amc.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.amc.bank.dto.AccountRequest;
import com.amc.bank.dto.CustomerRequest;
import com.amc.bank.dto.LoanRequest;
import com.amc.bank.model.Account;
import com.amc.bank.model.AppUser;
import com.amc.bank.model.Customer;
import com.amc.bank.model.Loan;
import com.amc.bank.model.Transaction;
import com.amc.bank.model.TransactionType;
import com.amc.bank.repository.AccountRepository;
import com.amc.bank.repository.CustomerRepository;
import com.amc.bank.repository.LoanRepository;
import com.amc.bank.repository.TransactionRepository;
import com.amc.bank.repository.UserRepository;

class BankServiceTest {

	private CustomerRepository customerRepository;
	private AccountRepository accountRepository;
	private LoanRepository loanRepository;
	private UserRepository userRepository;
	private TransactionRepository transactionRepository;
	private PasswordEncoder passwordEncoder;
	private BankService service;

	private Customer customer;

	@BeforeEach
	void setUp() {
		customerRepository = mock(CustomerRepository.class);
		accountRepository = mock(AccountRepository.class);
		loanRepository = mock(LoanRepository.class);
		userRepository = mock(UserRepository.class);
		transactionRepository = mock(TransactionRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		service = new BankService(customerRepository, accountRepository,
				loanRepository, userRepository, transactionRepository, passwordEncoder);
		customer = new Customer("Pritam", "pritam@example.com", "Kolkata", "ABCDE1234F");
		customer.setId(7L);
		lenient().when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));
		lenient().when(customerRepository.save(any(Customer.class)))
				.thenAnswer(inv -> inv.getArgument(0));
		lenient().when(accountRepository.save(any(Account.class)))
				.thenAnswer(inv -> inv.getArgument(0));
		lenient().when(loanRepository.save(any(Loan.class)))
				.thenAnswer(inv -> inv.getArgument(0));
	}

	private Account account(Long id, BigDecimal balance, String status) {
		Account account = new Account();
		account.setId(id);
		account.setAccountNumber("AC00000001");
		account.setType("SAVINGS");
		account.setBalance(balance);
		account.setStatus(status);
		account.setCustomer(customer);
		return account;
	}

	// -------------------- CUSTOMERS --------------------

	@Test
	void addCustomerCreatesLoginWithHashedPassword() {
		CustomerRequest request = new CustomerRequest();
		request.setName("  Pritam ");
		request.setEmail("Pritam@Example.com");
		request.setPanNumber(" abcde1234f ");
		request.setInitialPassword("secret1");
		when(userRepository.existsByUsernameIgnoreCase("pritam@example.com")).thenReturn(false);
		when(customerRepository.existsByEmail("pritam@example.com")).thenReturn(false);
		when(customerRepository.existsByPanNumber("ABCDE1234F")).thenReturn(false);
		when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
		when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
		when(passwordEncoder.encode("secret1")).thenReturn("$2a$encoded");

		Customer saved = service.addCustomer(request);

		assertEquals("pritam@example.com", saved.getEmail());
		assertEquals("ABCDE1234F", saved.getPanNumber());
		ArgumentCaptor<AppUser> loginCaptor = ArgumentCaptor.forClass(AppUser.class);
		verify(userRepository).save(loginCaptor.capture());
		assertEquals("pritam@example.com", loginCaptor.getValue().getUsername());
		assertEquals("$2a$encoded", loginCaptor.getValue().getPassword());
		assertEquals("ROLE_CUSTOMER", loginCaptor.getValue().getRole());
	}

	@Test
	void addCustomerRejectsShortPasswordAndDuplicateLogin() {
		CustomerRequest request = new CustomerRequest();
		request.setName("Pritam");
		request.setEmail("p@example.com");
		request.setPanNumber("ABCDE1234F");
		request.setInitialPassword("12345");
		assertThrows(IllegalArgumentException.class, () -> service.addCustomer(request));

		request.setInitialPassword("longenough");
		when(userRepository.existsByUsernameIgnoreCase("p@example.com")).thenReturn(true);
		assertThrows(IllegalArgumentException.class, () -> service.addCustomer(request));
	}

	@Test
	void updateCustomerSyncsLoginUsernameOnEmailChange() {
		AppUser login = new AppUser("pritam@example.com", "$2a$hash", "ROLE_CUSTOMER", customer);
		when(userRepository.findByCustomerId(7L)).thenReturn(Optional.of(login));
		when(customerRepository.existsByEmail("new@example.com")).thenReturn(false);
		when(userRepository.existsByUsernameIgnoreCase("new@example.com")).thenReturn(false);
		when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

		CustomerRequest request = new CustomerRequest();
		request.setName("Pritam");
		request.setEmail("new@example.com");
		request.setPanNumber("ABCDE1234F");

		Customer saved = service.updateCustomer(7L, request);

		assertEquals("new@example.com", saved.getEmail());
		assertEquals("new@example.com", login.getUsername());
	}

	@Test
	void deleteCustomerBlockedWhileAccountsOrLoansExist() {
		when(accountRepository.findByCustomerId(7L)).thenReturn(List.of(account(1L, BigDecimal.TEN, "ACTIVE")));
		when(loanRepository.findByCustomerId(7L)).thenReturn(List.of());
		assertThrows(IllegalArgumentException.class, () -> service.deleteCustomer(7L));
	}

	// -------------------- ACCOUNTS --------------------

	@Test
	void openAccountGeneratesNumberAndDefaultsBalance() {
		AccountRequest request = new AccountRequest();
		request.setType("savings");
		Account saved = service.openAccount(7L, request);
		assertEquals("SAVINGS", saved.getType());
		assertEquals("ACTIVE", saved.getStatus());
		assertEquals(BigDecimal.ZERO, saved.getBalance());
		assertTrue(saved.getAccountNumber().startsWith("AC"));
		assertEquals(10, saved.getAccountNumber().length());
	}

	@Test
	void openAccountRejectsBadTypeAndNegativeBalance() {
		AccountRequest badType = new AccountRequest();
		badType.setType("FIXED");
		assertThrows(IllegalArgumentException.class, () -> service.openAccount(7L, badType));

		AccountRequest negative = new AccountRequest();
		negative.setType("SAVINGS");
		negative.setBalance(new BigDecimal("-1"));
		assertThrows(IllegalArgumentException.class, () -> service.openAccount(7L, negative));
	}

	@Test
	void depositAddsToBalanceOnlyForActiveAccounts() {
		Account active = account(1L, new BigDecimal("100.00"), "ACTIVE");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(active));
		Account result = service.deposit(1L, new BigDecimal("50"));
		assertEquals(new BigDecimal("150.00"), result.getBalance());

		Account closed = account(2L, new BigDecimal("100.00"), "CLOSED");
		when(accountRepository.findById(2L)).thenReturn(Optional.of(closed));
		assertThrows(IllegalArgumentException.class, () -> service.deposit(2L, BigDecimal.ONE));
	}

	@Test
	void depositRejectsZeroNegativeAndNullAmounts() {
		Account active = account(1L, new BigDecimal("100.00"), "ACTIVE");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(active));
		assertThrows(IllegalArgumentException.class, () -> service.deposit(1L, BigDecimal.ZERO));
		assertThrows(IllegalArgumentException.class, () -> service.deposit(1L, new BigDecimal("-5")));
		assertThrows(IllegalArgumentException.class, () -> service.deposit(1L, null));
	}

	@Test
	void withdrawReducesBalanceButNeverBelowZero() {
		Account active = account(1L, new BigDecimal("100.00"), "ACTIVE");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(active));
		assertEquals(new BigDecimal("40.00"), service.withdraw(1L, new BigDecimal("60")).getBalance());
		assertThrows(IllegalArgumentException.class, () -> service.withdraw(1L, new BigDecimal("61")));
	}

	// -------------------- TRANSACTION HISTORY --------------------

	@Test
	void successfulDepositCreatesDepositTransactionWithBalanceAfter() {
		Account active = account(1L, new BigDecimal("100.00"), "ACTIVE");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(active));

		service.deposit(1L, new BigDecimal("50"));

		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
		verify(transactionRepository).save(captor.capture());
		Transaction recorded = captor.getValue();
		assertEquals(TransactionType.DEPOSIT, recorded.getType());
		assertEquals(new BigDecimal("50"), recorded.getAmount());
		assertEquals(new BigDecimal("150.00"), recorded.getBalanceAfterTransaction());
		assertEquals(active, recorded.getAccount());
	}

	@Test
	void successfulWithdrawalCreatesWithdrawalTransactionWithBalanceAfter() {
		Account active = account(1L, new BigDecimal("100.00"), "ACTIVE");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(active));

		service.withdraw(1L, new BigDecimal("60"));

		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
		verify(transactionRepository).save(captor.capture());
		Transaction recorded = captor.getValue();
		assertEquals(TransactionType.WITHDRAWAL, recorded.getType());
		assertEquals(new BigDecimal("60"), recorded.getAmount());
		assertEquals(new BigDecimal("40.00"), recorded.getBalanceAfterTransaction());
	}

	@Test
	void failedWithdrawalCreatesNoTransaction() {
		Account active = account(1L, new BigDecimal("100.00"), "ACTIVE");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(active));

		assertThrows(IllegalArgumentException.class,
				() -> service.withdraw(1L, new BigDecimal("101")));
		// The funds check runs before any write: no record is created, and the
		// balance was never touched either, so nothing can commit partially.
		verify(transactionRepository, never()).save(any(Transaction.class));
		assertEquals(new BigDecimal("100.00"), active.getBalance());
	}

	@Test
	void invalidDepositCreatesNoTransaction() {
		Account active = account(1L, new BigDecimal("100.00"), "ACTIVE");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(active));

		assertThrows(IllegalArgumentException.class, () -> service.deposit(1L, BigDecimal.ZERO));
		assertThrows(IllegalArgumentException.class, () -> service.deposit(1L, new BigDecimal("-5")));
		assertThrows(IllegalArgumentException.class, () -> service.deposit(1L, null));
		verify(transactionRepository, never()).save(any(Transaction.class));
		assertEquals(new BigDecimal("100.00"), active.getBalance());
	}

	@Test
	void selfServiceDepositAlsoRecordsTransaction() {
		AppUser login = new AppUser("pritam@example.com", "$2a$hash", "ROLE_CUSTOMER", customer);
		when(userRepository.findByUsernameIgnoreCase("pritam@example.com")).thenReturn(Optional.of(login));
		Account own = account(1L, new BigDecimal("100"), "ACTIVE");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(own));

		service.depositMyAccount("pritam@example.com", 1L, new BigDecimal("25"));

		ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
		verify(transactionRepository).save(captor.capture());
		assertEquals(TransactionType.DEPOSIT, captor.getValue().getType());
		assertEquals(new BigDecimal("125"), captor.getValue().getBalanceAfterTransaction());
	}

	@Test
	void transactionHistoryRequiresOwnershipOfTheAccount() {
		AppUser login = new AppUser("pritam@example.com", "$2a$hash", "ROLE_CUSTOMER", customer);
		when(userRepository.findByUsernameIgnoreCase("pritam@example.com")).thenReturn(Optional.of(login));
		Customer stranger = new Customer("Other", "other@example.com", "Delhi", "ZZZZZ9999Z");
		stranger.setId(99L);
		Account foreignAccount = account(1L, new BigDecimal("100"), "ACTIVE");
		foreignAccount.setCustomer(stranger);
		when(accountRepository.findById(1L)).thenReturn(Optional.of(foreignAccount));

		assertThrows(IllegalArgumentException.class,
				() -> service.getTransactionHistory("pritam@example.com", 1L, PageRequest.of(0, 10)));
		verify(transactionRepository, never()).findByAccountIdOrderByTimestampDesc(any(), any());
	}

	@Test
	void transactionHistoryDelegatesPagedQueryForOwnedAccount() {
		AppUser login = new AppUser("pritam@example.com", "$2a$hash", "ROLE_CUSTOMER", customer);
		when(userRepository.findByUsernameIgnoreCase("pritam@example.com")).thenReturn(Optional.of(login));
		when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1L, new BigDecimal("100"), "ACTIVE")));
		Page<Transaction> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
		when(transactionRepository.findByAccountIdOrderByTimestampDesc(1L, PageRequest.of(0, 10)))
				.thenReturn(emptyPage);

		Page<Transaction> result = service.getTransactionHistory(
				"pritam@example.com", 1L, PageRequest.of(0, 10));

		assertEquals(0, result.getTotalElements());
	}

	// -------------------- SELF-SERVICE OWNERSHIP --------------------

	@Test
	void selfServiceDepositRequiresOwnership() {
		AppUser login = new AppUser("pritam@example.com", "$2a$hash", "ROLE_CUSTOMER", customer);
		when(userRepository.findByUsernameIgnoreCase("pritam@example.com")).thenReturn(Optional.of(login));

		Customer stranger = new Customer("Other", "other@example.com", "Delhi", "ZZZZZ9999Z");
		stranger.setId(99L);
		Account foreignAccount = account(1L, new BigDecimal("100"), "ACTIVE");
		foreignAccount.setCustomer(stranger);
		when(accountRepository.findById(1L)).thenReturn(Optional.of(foreignAccount));

		assertThrows(IllegalArgumentException.class,
				() -> service.depositMyAccount("pritam@example.com", 1L, BigDecimal.TEN));
		assertThrows(IllegalArgumentException.class,
				() -> service.withdrawMyAccount("pritam@example.com", 1L, BigDecimal.TEN));
	}

	@Test
	void selfServiceWithdrawEnforcesBalanceAndOwnership() {
		AppUser login = new AppUser("pritam@example.com", "$2a$hash", "ROLE_CUSTOMER", customer);
		when(userRepository.findByUsernameIgnoreCase("pritam@example.com")).thenReturn(Optional.of(login));
		Account own = account(1L, new BigDecimal("100"), "ACTIVE");
		when(accountRepository.findById(1L)).thenReturn(Optional.of(own));

		assertEquals(new BigDecimal("90"), service.withdrawMyAccount("pritam@example.com", 1L, BigDecimal.TEN).getBalance());
		assertThrows(IllegalArgumentException.class,
				() -> service.withdrawMyAccount("pritam@example.com", 1L, new BigDecimal("101")));
	}

	@Test
	void changeCustomerPasswordValidatesCurrentAndDifference() {
		AppUser login = new AppUser("pritam@example.com", "$2a$hash", "ROLE_CUSTOMER", customer);
		when(userRepository.findByUsernameIgnoreCase("pritam@example.com")).thenReturn(Optional.of(login));
		when(passwordEncoder.matches("current", "$2a$hash")).thenReturn(true);
		when(passwordEncoder.matches("brandnew", "$2a$hash")).thenReturn(false);
		when(passwordEncoder.encode("brandnew")).thenReturn("brandnew-hash");

		assertThrows(IllegalArgumentException.class,
				() -> service.changeCustomerPassword("pritam@example.com", "wrong", "brandnew"));
		assertThrows(IllegalArgumentException.class,
				() -> service.changeCustomerPassword("pritam@example.com", "current", "cur"));
		assertThrows(IllegalArgumentException.class,
				() -> service.changeCustomerPassword("pritam@example.com", "current", "current"));

		service.changeCustomerPassword("pritam@example.com", "current", "brandnew");
		ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
		verify(userRepository).save(captor.capture());
		assertEquals("brandnew-hash", captor.getValue().getPassword());
	}

	// -------------------- LOANS --------------------

	private LoanRequest loanRequest(String type, String principal, double rate, int months) {
		LoanRequest request = new LoanRequest();
		request.setType(type);
		request.setPrincipal(new BigDecimal(principal));
		request.setInterestRate(rate);
		request.setTenureMonths(months);
		return request;
	}

	@Test
	void sanctionLoanValidatesAllEligibilityRules() {
		assertThrows(IllegalArgumentException.class,
				() -> service.sanctionLoan(7L, loanRequest("HOME", "9999.99", 10, 12)));
		assertThrows(IllegalArgumentException.class,
				() -> service.sanctionLoan(7L, loanRequest("HOME", "50000", 0, 12)));
		assertThrows(IllegalArgumentException.class,
				() -> service.sanctionLoan(7L, loanRequest("HOME", "50000", 50.1, 12)));
		assertThrows(IllegalArgumentException.class,
				() -> service.sanctionLoan(7L, loanRequest("HOME", "50000", 10, 481)));
		assertThrows(IllegalArgumentException.class,
				() -> service.sanctionLoan(7L, loanRequest("HOME", "50000", 10, 0)));
		assertThrows(IllegalArgumentException.class,
				() -> service.sanctionLoan(7L, loanRequest("  ", "50000", 10, 12)));
	}

	@Test
	void sanctionLoanEnforcesMaxThreeActiveLoans() {
		when(loanRepository.countByCustomerIdAndStatus(7L, "ACTIVE")).thenReturn(3L);
		when(accountRepository.findByCustomerId(7L))
				.thenReturn(List.of(account(1L, new BigDecimal("100000"), "ACTIVE")));
		assertThrows(IllegalArgumentException.class,
				() -> service.sanctionLoan(7L, loanRequest("HOME", "50000", 10, 12)));
	}

	@Test
	void sanctionLoanRequiresBalanceAtLeastTenPercentOfPrincipal() {
		when(loanRepository.countByCustomerIdAndStatus(7L, "ACTIVE")).thenReturn(0L);
		when(accountRepository.findByCustomerId(7L))
				.thenReturn(List.of(account(1L, new BigDecimal("4999.99"), "ACTIVE")));
		assertThrows(IllegalArgumentException.class,
				() -> service.sanctionLoan(7L, loanRequest("HOME", "50000", 10, 12)));
		// Inactive accounts must not count toward the balance: a large FROZEN
		// balance alone cannot satisfy the 10% rule.
		when(accountRepository.findByCustomerId(7L))
				.thenReturn(List.of(account(2L, new BigDecimal("100000"), "FROZEN")));
		assertThrows(IllegalArgumentException.class,
				() -> service.sanctionLoan(7L, loanRequest("CAR", "40000", 10, 12)));
	}

	@Test
	void sanctionLoanSavesActiveLoanWithEmi() {
		when(loanRepository.countByCustomerIdAndStatus(7L, "ACTIVE")).thenReturn(2L);
		when(accountRepository.findByCustomerId(7L))
				.thenReturn(List.of(account(1L, new BigDecimal("100000"), "ACTIVE")));
		when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

		Loan loan = service.sanctionLoan(7L, loanRequest("HOME", "100000", 12, 12));

		assertEquals("ACTIVE", loan.getStatus());
		assertEquals(customer, loan.getCustomer());
		assertEquals(new BigDecimal("8884.88"), loan.getEmi());
	}

	// -------------------- DASHBOARD --------------------

	@Test
	void dashboardCountsComeFromRepositories() {
		when(customerRepository.count()).thenReturn(42L);
		when(accountRepository.countByStatus("ACTIVE")).thenReturn(7L);
		when(loanRepository.countByStatus("ACTIVE")).thenReturn(3L);
		assertEquals(42L, service.customerCount());
		assertEquals(7L, service.activeAccountCount());
		assertEquals(3L, service.activeLoanCount());
	}

	@Test
	void customerSearchDelegatesToRepository() {
		when(customerRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase("pri", "pri"))
				.thenReturn(List.of(customer));
		when(customerRepository.findAll()).thenReturn(List.of(customer));
		assertEquals(1, service.getCustomers("pri").size());
		// Blank search must fall back to listing all customers.
		assertEquals(1, service.getCustomers(" ").size());
	}
}
