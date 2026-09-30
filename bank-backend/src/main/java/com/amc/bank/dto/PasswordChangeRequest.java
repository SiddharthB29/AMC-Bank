package com.amc.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Customer self-service password change request. */
public class PasswordChangeRequest {

	@NotBlank
	private String currentPassword;

	@NotBlank
	@Size(min = 6, message = "Customer password must contain at least 6 characters")
	private String newPassword;

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
