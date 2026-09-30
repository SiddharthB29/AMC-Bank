package com.amc.bank.dto;

import java.util.List;

/** Response for /api/auth/me: username and granted authorities only. */
public class AuthMeResponse {

	private final String username;
	private final List<String> authorities;

	public AuthMeResponse(String username, List<String> authorities) {
		this.username = username;
		this.authorities = authorities;
	}

	public String getUsername() {
		return username;
	}

	public List<String> getAuthorities() {
		return authorities;
	}
}
