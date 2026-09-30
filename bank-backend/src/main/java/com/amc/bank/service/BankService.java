package com.amc.bank.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * Core banking business logic, ported from the reference AMC Bank backend.
 *
 * <p>Customer operations (deposit/withdraw/change password) resolve the
 * authenticated identity by username and re-check account ownership, so no
 * customer-supplied id is ever trusted on its own.</p>
 */
@Service
public class BankService {

	private static final String STATUS_ACTIVE = "ACTIVE";
	private static final BigDecimal MIN_LOAN_PRINCIPAL = new BigDecimal("10000");
	private static final BigDecimal LOAN_BALANCE_RATIO = new BigDecimal("0.10");
	private static final int MAX_ACTIVE_LOANS = 3;
	private static final int MAX_TENURE_MONTHS = 480;
	private static final double MAX_INTEREST_RATE = 50;

	private final CustomerRepository customerRepository;
	private final AccountRepository accountRepository;
	private final LoanRepository loanRepository;
	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;
	private final PasswordEncoder passwordEncoder;

	public BankService(CustomerRepository customerRepository,
			AccountRepository accountRepository,
			LoanRepository loanRepository,
			UserRepository userRepository,
			TransactionRepository transactionRepository,
			PasswordEncoder passwordEncoder) {
		this.customerRepository = customerRepository;
		this.accountRepository = accountRepository;
		this.loanRepository = loanRepository;
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
		this.passwordEncoder = passwordEncoder;
	}

	// -------------------- CUSTOMERS (ADMIN) --------------------

	public List<Customer> getCustomers(String search) {
		if (search == null || search.isBlank()) {
			return customerRepository.findAll();
		}
		return customerRepository
				.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(search, search);
	}

	public Customer getCustomer(Long id) {
		return customerRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Customer not found"));
	}

	// Admin creates the customer and the customer's first login password together.
	// The customer email becomes the login username.
	@Transactional
	public Customer addCustomer(CustomerRequest request) {
		Customer customer = new Customer(
				request.getName(),
				request.getEmail(),
				request.getCity(),
				request.getPanNumber());
		normalizeCustomer(customer);
		validateCustomer(customer, null);
		validateNewPassword(request.getInitialPassword());
		if (userRepository.existsByUsernameIgnoreCase(customer.getEmail())) {
			throw new IllegalArgumentException("A login already exists for this email");
		}
		Customer saved = customerRepository.save(customer);
		AppUser login = new AppUser(
				saved.getEmail(),
				passwordEncoder.encode(request.getInitialPassword()),
				"ROLE_CUSTOMER",
				saved);
		userRepository.save(login);
		return saved;
	}

	@Transactional
	public Customer updateCustomer(Long id, CustomerRequest request) {
		Customer customer = getCustomer(id);
		Customer input = new Customer(
				request.getName(),
				request.getEmail(),
				request.getCity(),
				request.getPanNumber());
		normalizeCustomer(input);
		validateCustomer(input, customer);
		String oldEmail = customer.getEmail();
		boolean emailChanged = !input.getEmail().equalsIgnoreCase(oldEmail);
		if (emailChanged && userRepository.existsByUsernameIgnoreCase(input.getEmail())) {
			throw new IllegalArgumentException("A login already exists for this email");
		}
		customer.setName(input.getName());
		customer.setEmail(input.getEmail());
		customer.setCity(input.getCity());
		customer.setPanNumber(input.getPanNumber());
		Customer saved = customerRepository.save(customer);
		// Keep the customer login id synchronized when an admin changes the email.
		if (emailChanged) {
			AppUser login = userRepository.findByCustomerId(id)
					.orElseThrow(() -> new IllegalArgumentException(
							"Customer login account not found"));
			login.setUsername(saved.getEmail());
			userRepository.save(login);
		}
		return saved;
	}

	@Transactional
	public void deleteCustomer(Long id) {
		getCustomer(id);
		if (!accountRepository.findByCustomerId(id).isEmpty()
				|| !loanRepository.findByCustomerId(id).isEmpty()) {
			throw new IllegalArgumentException(
					"Customer cannot be deleted while accounts or loans exist");
		}
		userRepository.findByCustomerId(id).ifPresent(user -> {
			userRepository.delete(user);
			userRepository.flush();
		});
		customerRepository.deleteById(id);
	}

	private void normalizeCustomer(Customer customer) {
		if (customer.getName() != null) {
			customer.setName(customer.getName().trim());
		}
		if (customer.getEmail() != null) {
			customer.setEmail(customer.getEmail().trim().toLowerCase(Locale.ROOT));
		}
		if (customer.getCity() != null) {
			customer.setCity(customer.getCity().trim());
		}
		if (customer.getPanNumber() != null) {
			customer.setPanNumber(customer.getPanNumber().trim().toUpperCase(Locale.ROOT));
		}
	}

	private void validateCustomer(Customer input, Customer existing) {
		if (input.getName() == null || input.getName().isBlank()) {
			throw new IllegalArgumentException("Customer name is required");
		}
		if (input.getEmail() == null || !input.getEmail().contains("@")) {
			throw new IllegalArgumentException("Valid email is required");
		}
		if (input.getPanNumber() == null || input.getPanNumber().isBlank()) {
			throw new IllegalArgumentException("PAN number is required");
		}
		boolean emailChanged = existing == null
				|| !input.getEmail().equalsIgnoreCase(existing.getEmail());
		boolean panChanged = existing == null
				|| !input.getPanNumber().equalsIgnoreCase(existing.getPanNumber());
		if (emailChanged && customerRepository.existsByEmail(input.getEmail())) {
			throw new IllegalArgumentException("Email already registered");
		}
		if (panChanged && customerRepository.existsByPanNumber(input.getPanNumber())) {
			throw new IllegalArgumentException("PAN already registered");
		}
	}

	private void validateNewPassword(String password) {
		if (password == null || password.length() < 6) {
			throw new IllegalArgumentException(
					"Customer password must contain at least 6 characters");
		}
	}

	// -------------------- CUSTOMER SELF-SERVICE --------------------

	/**
	 * Resolves the logged-in customer from the authenticated username (the
	 * login identity; the customer's email for customer logins). Customer
	 * endpoints derive identity here instead of trusting request ids.
	 */
	public Customer getCustomerForUsername(String username) {
		AppUser user = userRepository.findByUsernameIgnoreCase(username)
				.orElseThrow(() -> new IllegalArgumentException("Login account not found"));
		if (user.getCustomer() == null) {
			throw new IllegalArgumentException("No customer is linked to this login");
		}
		return user.getCustomer();
	}

	public List<Account> getMyAccounts(String username) {
		Customer customer = getCustomerForUsername(username);
		return accountRepository.findByCustomerId(customer.getId());
	}

	/**
	 * Transaction history for one of the logged-in customer's own accounts,
	 * newest first. Ownership is enforced here: the requested account must
	 * belong to the customer resolved from the authenticated username.
	 */
	public Page<Transaction> getTransactionHistory(String username, Long accountId,
			Pageable pageable) {
		getOwnedActiveAccount(username, accountId);
		return transactionRepository.findByAccountIdOrderByTimestampDesc(accountId, pageable);
	}

	public List<Loan> getMyLoans(String username) {
		Customer customer = getCustomerForUsername(username);
		return loanRepository.findByCustomerId(customer.getId());
	}

	// Customer self-service deposit. The account must belong to the logged-in customer.
	@Transactional
	public Account depositMyAccount(String username, Long accountId, BigDecimal amount) {
		Account account = getOwnedActiveAccount(username, accountId);
		validatePositiveAmount(amount);
		return applyDeposit(account, amount);
	}

	// Customer self-service withdrawal. The requested amount must be less than
	// or equal to the available balance; negative balances are never allowed.
	@Transactional
	public Account withdrawMyAccount(String username, Long accountId, BigDecimal amount) {
		Account account = getOwnedActiveAccount(username, accountId);
		validatePositiveAmount(amount);
		return applyWithdrawal(account, amount);
	}

	private Account getOwnedActiveAccount(String username, Long accountId) {
		Customer customer = getCustomerForUsername(username);
		Account account = getActiveAccount(accountId);
		if (account.getCustomer() == null
				|| !account.getCustomer().getId().equals(customer.getId())) {
			throw new IllegalArgumentException(
					"You can perform transactions only on your own account");
		}
		return account;
	}

	@Transactional
	public void changeCustomerPassword(String username,
			String currentPassword,
			String newPassword) {
		AppUser user = userRepository.findByUsernameIgnoreCase(username)
				.orElseThrow(() -> new IllegalArgumentException("Login account not found"));
		if (!"ROLE_CUSTOMER".equals(user.getRole()) || user.getCustomer() == null) {
			throw new IllegalArgumentException("Password change is available to customers here");
		}
		if (currentPassword == null
				|| !passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}
		validateNewPassword(newPassword);
		if (passwordEncoder.matches(newPassword, user.getPassword())) {
			throw new IllegalArgumentException(
					"New password must be different from the current password");
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	// -------------------- ACCOUNTS --------------------

	public List<Account> getAccounts(Long customerId) {
		getCustomer(customerId);
		return accountRepository.findByCustomerId(customerId);
	}

	@Transactional
	public Account openAccount(Long customerId, AccountRequest request) {
		Customer customer = getCustomer(customerId);
		if (request.getType() == null || !(request.getType().equalsIgnoreCase("SAVINGS")
				|| request.getType().equalsIgnoreCase("CURRENT"))) {
			throw new IllegalArgumentException("Account type must be SAVINGS or CURRENT");
		}
		BigDecimal openingBalance = request.getBalance() == null
				? BigDecimal.ZERO
				: request.getBalance();
		if (openingBalance.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Opening balance cannot be negative");
		}
		Account account = new Account();
		account.setAccountNumber("AC" + UUID.randomUUID().toString()
				.replace("-", "").substring(0, 8).toUpperCase());
		account.setType(request.getType().toUpperCase());
		account.setBalance(openingBalance);
		account.setStatus(STATUS_ACTIVE);
		account.setCustomer(customer);
		return accountRepository.save(account);
	}

	@Transactional
	public Account deposit(Long accountId, BigDecimal amount) {
		Account account = getActiveAccount(accountId);
		validatePositiveAmount(amount);
		return applyDeposit(account, amount);
	}

	@Transactional
	public Account withdraw(Long accountId, BigDecimal amount) {
		Account account = getActiveAccount(accountId);
		validatePositiveAmount(amount);
		return applyWithdrawal(account, amount);
	}

	/**
	 * Applies a validated deposit: balance increase plus DEPOSIT record,
	 * committed atomically by the caller's transaction. Never called after a
	 * validation failure, so no record can exist for an invalid request.
	 */
	private Account applyDeposit(Account account, BigDecimal amount) {
		account.setBalance(account.getBalance().add(amount));
		transactionRepository.save(new Transaction(
				account, TransactionType.DEPOSIT, amount, account.getBalance()));
		return account;
	}

	/**
	 * Applies a validated withdrawal: the funds check runs first (failure
	 * creates no record), then the balance decrease plus WITHDRAWAL record,
	 * committed atomically by the caller's transaction.
	 */
	private Account applyWithdrawal(Account account, BigDecimal amount) {
		if (amount.compareTo(account.getBalance()) > 0) {
			throw new IllegalArgumentException(
					"Withdrawal amount cannot exceed available balance of "
							+ account.getBalance());
		}
		account.setBalance(account.getBalance().subtract(amount));
		transactionRepository.save(new Transaction(
				account, TransactionType.WITHDRAWAL, amount, account.getBalance()));
		return account;
	}

	private Account getActiveAccount(Long id) {
		Account account = accountRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Account not found"));
		if (!STATUS_ACTIVE.equalsIgnoreCase(account.getStatus())) {
			throw new IllegalArgumentException("Account is not active");
		}
		return account;
	}

	private void validatePositiveAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be greater than zero");
		}
	}

	// -------------------- LOANS --------------------

	public List<Loan> getAllLoans() {
		return loanRepository.findAll();
	}

	public List<Loan> getLoans(Long customerId) {
		getCustomer(customerId);
		return loanRepository.findByCustomerId(customerId);
	}

	@Transactional
	public Loan sanctionLoan(Long customerId, LoanRequest request) {
		Customer customer = getCustomer(customerId);
		if (request.getPrincipal() == null
				|| request.getPrincipal().compareTo(MIN_LOAN_PRINCIPAL) < 0) {
			throw new IllegalArgumentException("Loan principal must be at least 10000");
		}
		if (request.getType() == null || request.getType().isBlank()) {
			throw new IllegalArgumentException("Loan type is required");
		}
		if (request.getTenureMonths() <= 0 || request.getTenureMonths() > MAX_TENURE_MONTHS) {
			throw new IllegalArgumentException("Tenure must be between 1 and 480 months");
		}
		if (request.getInterestRate() <= 0 || request.getInterestRate() > MAX_INTEREST_RATE) {
			throw new IllegalArgumentException("Interest rate must be between 0 and 50");
		}
		if (loanRepository.countByCustomerIdAndStatus(customerId, STATUS_ACTIVE) >= MAX_ACTIVE_LOANS) {
			throw new IllegalArgumentException("Customer already has 3 active loans");
		}
		BigDecimal totalBalance = accountRepository.findByCustomerId(customerId)
				.stream()
				.filter(a -> STATUS_ACTIVE.equalsIgnoreCase(a.getStatus()))
				.map(Account::getBalance)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal requiredBalance = request.getPrincipal().multiply(LOAN_BALANCE_RATIO);
		if (totalBalance.compareTo(requiredBalance) < 0) {
			throw new IllegalArgumentException(
					"Customer balance must be at least 10% of loan principal");
		}
		Loan loan = new Loan();
		loan.setType(request.getType());
		loan.setPrincipal(request.getPrincipal());
		loan.setInterestRate(request.getInterestRate());
		loan.setTenureMonths(request.getTenureMonths());
		loan.setCustomer(customer);
		loan.setStatus(STATUS_ACTIVE);
		loan.calculateEmi();
		return loanRepository.save(loan);
	}

	// -------------------- DASHBOARD --------------------

	public long customerCount() {
		return customerRepository.count();
	}

	public long activeAccountCount() {
		return accountRepository.countByStatus(STATUS_ACTIVE);
	}

	public long activeLoanCount() {
		return loanRepository.countByStatus(STATUS_ACTIVE);
	}
}
