package com.amc.bank.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A bank account owned by exactly one customer.
 *
 * <p>Type is SAVINGS or CURRENT and status is ACTIVE or a closed/inactive
 * value; both are validated in the service layer, as in the reference.</p>
 */
@Entity
@Table(name = "accounts")
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Generated at account opening, e.g. "AC" + 8 hex characters.
	 */
	@Column(unique = true, nullable = false)
	private String accountNumber;

	/**
	 * SAVINGS or CURRENT.
	 */
	@Column
	private String type;

	/**
	 * Current balance; deposits add and withdrawals subtract. Never negative.
	 */
	@Column(precision = 19, scale = 2, nullable = false)
	private BigDecimal balance = BigDecimal.ZERO;

	/**
	 * ACTIVE or an inactive/closed status.
	 */
	@Column
	private String status = "ACTIVE";

	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	@JsonIgnore
	private Customer customer;

	public Account() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}
