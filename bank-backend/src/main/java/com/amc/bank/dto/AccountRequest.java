package com.amc.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

/** Account opening request; balance is optional and defaults to zero. */
public class AccountRequest {

	@NotBlank
	private String type; // SAVINGS or CURRENT

	@DecimalMin(value = "0.00", message = "Opening balance cannot be negative")
	private BigDecimal balance;

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
}
