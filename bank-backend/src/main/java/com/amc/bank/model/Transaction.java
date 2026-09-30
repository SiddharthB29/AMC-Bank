package com.amc.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Immutable history record of a single successful deposit or withdrawal on an
 * account. Records are only created inside the same database transaction that
 * applies the balance change, so the ledger and balances cannot drift apart.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * The account this transaction belongs to. A transaction belongs to
	 * exactly one account; one account has many transactions.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	/**
	 * DEPOSIT or WITHDRAWAL.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TransactionType type;

	/**
	 * The deposited or withdrawn amount (always positive).
	 */
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	/**
	 * Account balance immediately after this transaction was applied.
	 */
	@Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
	private BigDecimal balanceAfterTransaction;

	/**
	 * Set automatically on first persistence; never accepted from API clients.
	 */
	@Column(nullable = false, updatable = false)
	private LocalDateTime timestamp;

	public Transaction() {
	}

	public Transaction(Account account, TransactionType type, BigDecimal amount,
			BigDecimal balanceAfterTransaction) {
		this.account = account;
		this.type = type;
		this.amount = amount;
		this.balanceAfterTransaction = balanceAfterTransaction;
	}

	@PrePersist
	void onCreate() {
		if (timestamp == null) {
			timestamp = LocalDateTime.now();
		}
	}

	public Long getId() {
		return id;
	}

	public Account getAccount() {
		return account;
	}

	public TransactionType getType() {
		return type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getBalanceAfterTransaction() {
		return balanceAfterTransaction;
	}

	/**
	 * Read-only by design: the timestamp is assigned by @PrePersist and never
	 * mutable through API input.
	 */
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}
