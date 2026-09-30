package com.amc.bank.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Loan sanction request; constraints mirror the reference banking rules. */
public class LoanRequest {

	@NotBlank
	private String type; // HOME, CAR or PERSONAL

	@NotNull
	@DecimalMin(value = "10000", message = "Loan principal must be at least 10000")
	private BigDecimal principal;

	@DecimalMin(value = "0.0", inclusive = false, message = "Interest rate must be greater than 0")
	@DecimalMax(value = "50.0", message = "Interest rate must be at most 50")
	private double interestRate;

	@Min(value = 1, message = "Tenure must be between 1 and 480 months")
	@Max(value = 480, message = "Tenure must be between 1 and 480 months")
	private int tenureMonths;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getPrincipal() {
		return principal;
	}

	public void setPrincipal(BigDecimal principal) {
		this.principal = principal;
	}

	public double getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(double interestRate) {
		this.interestRate = interestRate;
	}

	public int getTenureMonths() {
		return tenureMonths;
	}

	public void setTenureMonths(int tenureMonths) {
		this.tenureMonths = tenureMonths;
	}
}
