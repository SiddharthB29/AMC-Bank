package com.amc.bank.dto;

/** Admin dashboard counters. */
public class DashboardResponse {

	private final long customers;
	private final long activeAccounts;
	private final long activeLoans;

	public DashboardResponse(long customers, long activeAccounts, long activeLoans) {
		this.customers = customers;
		this.activeAccounts = activeAccounts;
		this.activeLoans = activeLoans;
	}

	public long getCustomers() {
		return customers;
	}

	public long getActiveAccounts() {
		return activeAccounts;
	}

	public long getActiveLoans() {
		return activeLoans;
	}
}
