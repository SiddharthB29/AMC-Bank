package com.amc.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.amc.bank.model.Transaction;

/**
 * Public shape of a transaction-history entry: no account/customer identity,
 * no password material, no security-sensitive internals.
 */
public class TransactionResponse {

	private Long transactionId;
	private String type;
	private BigDecimal amount;
	private BigDecimal balanceAfterTransaction;
	private LocalDateTime timestamp;

	public TransactionResponse() {
	}

	public TransactionResponse(Long transactionId, String type, BigDecimal amount,
			BigDecimal balanceAfterTransaction, LocalDateTime timestamp) {
		this.transactionId = transactionId;
		this.type = type;
		this.amount = amount;
		this.balanceAfterTransaction = balanceAfterTransaction;
		this.timestamp = timestamp;
	}

	public static TransactionResponse from(Transaction transaction) {
		return new TransactionResponse(
				transaction.getId(),
				transaction.getType().name(),
				transaction.getAmount(),
				transaction.getBalanceAfterTransaction(),
				transaction.getTimestamp());
	}

	public Long getTransactionId() {
		return transactionId;
	}

	public String getType() {
		return type;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public BigDecimal getBalanceAfterTransaction() {
		return balanceAfterTransaction;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public void setBalanceAfterTransaction(BigDecimal balanceAfterTransaction) {
		this.balanceAfterTransaction = balanceAfterTransaction;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
}
