package com.amc.bank.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Customer create/update request (admin). initialPassword is accepted from
 * JSON only when an admin creates the customer; it is never persisted or
 * echoed back in responses.
 */
public class CustomerRequest {

	@NotBlank
	private String name;

	@NotBlank
	@Email
	private String email;

	private String city;

	@NotBlank
	private String panNumber;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Size(min = 6, message = "Customer password must contain at least 6 characters")
	private String initialPassword;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPanNumber() {
		return panNumber;
	}

	public void setPanNumber(String panNumber) {
		this.panNumber = panNumber;
	}

	public String getInitialPassword() {
		return initialPassword;
	}

	public void setInitialPassword(String initialPassword) {
		this.initialPassword = initialPassword;
	}
}
