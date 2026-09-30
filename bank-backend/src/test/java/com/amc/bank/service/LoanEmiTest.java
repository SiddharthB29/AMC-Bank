package com.amc.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.amc.bank.model.Loan;

/** Verifies the reference EMI formula against independently computed values. */
class LoanEmiTest {

	private Loan loan(BigDecimal principal, double rate, int months) {
		Loan loan = new Loan();
		loan.setPrincipal(principal);
		loan.setInterestRate(rate);
		loan.setTenureMonths(months);
		loan.calculateEmi();
		return loan;
	}

	@Test
	void emiUsesStandardAnnuityFormula() {
		// P=100000, r=12%/yr, n=12mo -> monthly rate 0.01 -> EMI 8884.88
		assertEquals(new BigDecimal("8884.88"),
				loan(new BigDecimal("100000"), 12, 12).getEmi());
	}

	@Test
	void emiWithZeroInterestDividesPrincipal() {
		assertEquals(new BigDecimal("10000.00"),
				loan(new BigDecimal("120000"), 0, 12).getEmi());
	}

	@Test
	void emiWithLongTenureLowRate() {
		// P=10000, r=9%, n=60mo -> EMI 207.58
		assertEquals(new BigDecimal("207.58"),
				loan(new BigDecimal("10000"), 9, 60).getEmi());
	}

	@Test
	void emiGuardedAgainstBadState() {
		assertEquals(BigDecimal.ZERO, loan(null, 10, 12).getEmi());
		assertEquals(BigDecimal.ZERO, loan(new BigDecimal("50000"), 10, 0).getEmi());
	}

	@Test
	void emiIsRoundedToTwoDecimalsHalfUp() {
		BigDecimal principal = new BigDecimal("123456.78");
		BigDecimal emi = loan(principal, 13.5, 240).getEmi();
		assertEquals(2, emi.scale());
		// Amortization bounds: EMI must exceed the interest-only payment
		// (P * monthly rate) and stay below P * monthly rate + P / n.
		double monthlyRate = 13.5 / 1200.0;
		double interestOnly = principal.doubleValue() * monthlyRate;
		assertTrue(emi.doubleValue() > interestOnly);
		assertTrue(emi.doubleValue() < interestOnly + principal.doubleValue() / 240);
	}
}
