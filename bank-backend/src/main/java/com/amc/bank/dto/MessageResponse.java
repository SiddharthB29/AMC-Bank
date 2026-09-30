package com.amc.bank.dto;

/** Simple message payload for informational/error responses. */
public class MessageResponse {

	private final String message;

	public MessageResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
