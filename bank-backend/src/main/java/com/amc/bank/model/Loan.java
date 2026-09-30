package com.amc.bank.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
 * A loan sanctioned to a customer.
 *
 * <p>Type is HOME, CAR or PERSONAL and status is ACTIVE or a closed value;
 * both are validated in the service layer, as in the reference.</p>
 */
@Entity
@Table(name = "loans")
public class Loan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * HOME, CAR or PERSONAL.
	 */
	@Column
	private String type;

	/**
	 * Sanctioned principal amount.
	 */
	@Column(precision = 19, scale = 2, nullable = false)
	private BigDecimal principal;

	/**
	 * Annual interest rate as a percentage, e.g. 9.5 for 9.5%.
	 */
	@Column
	private double interestRate;

	/**
	 * Repayment tenure in months (1-480).
	 */
	@Column
	private int tenureMonths;

	/**
	 * Equated monthly installment, computed once at sanction time.
	 */
	@Column(precision = 19, scale = 2)
	private BigDecimal emi;

	/**
	 * ACTIVE or a closed/paid-off status.
	 */
	@Column
	private String status = "ACTIVE";

	@ManyToOne
	@JoinColumn(name = "customer_id", nullable = false)
	@JsonIgnore
	private Customer customer;

	public Loan() {
	}

	/**
	 * Computes the EMI using the standard annuity formula
	 * P·r·(1+r)^n / ((1+r)^n − 1), where r is the monthly rate
	 * (annual rate / 1200) and n is the tenure in months.
	 *
	 * <p>Called by the service layer when a loan is sanctioned; the result is
	 * persisted in {@link #emi}.</p>
	 */
	public void calculateEmi() {
		if (principal == null || tenureMonths <= 0) {
			emi = BigDecimal.ZERO;
			return;
		}
		if (interestRate == 0) {
			emi = principal.divide(
					BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
			return;
		}
		double p = principal.doubleValue();
		double monthlyRate = interestRate / 1200.0;
		double factor = Math.pow(1 + monthlyRate, tenureMonths);
		double result = p * monthlyRate * factor / (factor - 1);
		emi = BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public BigDecimal getEmi() {
		return emi;
	}

	public void setEmi(BigDecimal emi) {
		this.emi = emi;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	// Read-only properties so the Loan JSON payload can show the owning
	// customer without exposing the full Customer object (the reference's
	// approach). The customer field itself remains @JsonIgnore.
	public Long getCustomerId() {
		return customer == null ? null : customer.getId();
	}

	public String getCustomerName() {
		return customer == null ? null : customer.getName();
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
}
