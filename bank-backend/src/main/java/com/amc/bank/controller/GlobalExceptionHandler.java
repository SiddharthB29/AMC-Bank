package com.amc.bank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.amc.bank.dto.MessageResponse;

/**
 * Global error handling: clean JSON messages for expected failures, no stack
 * traces, and no leakage of credentials, secrets, or security internals.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	// Domain validation failures from the service layer (the reference's
	// IllegalArgumentException messages are part of its API contract).
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<MessageResponse> handleBadRequest(IllegalArgumentException ex) {
		return ResponseEntity.badRequest()
				.body(new MessageResponse(ex.getMessage()));
	}

	// @Valid request-body failures: first field error message.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<MessageResponse> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getDefaultMessage())
				.orElse("Invalid request");
		return ResponseEntity.badRequest().body(new MessageResponse(message));
	}

	// Malformed JSON body.
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<MessageResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest()
				.body(new MessageResponse("Malformed request body"));
	}

	// Missing @RequestParam, e.g. deposit/withdraw without amount.
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<MessageResponse> handleMissingParam(
			MissingServletRequestParameterException ex) {
		return ResponseEntity.badRequest()
				.body(new MessageResponse("Missing required parameter: " + ex.getParameterName()));
	}

	// Non-numeric path or parameter values, e.g. /api/customers/abc.
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<MessageResponse> handleTypeMismatch(
			MethodArgumentTypeMismatchException ex) {
		return ResponseEntity.badRequest()
				.body(new MessageResponse("Invalid value for " + ex.getName()));
	}

	// Unknown URL.
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<MessageResponse> handleNotFound(NoResourceFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new MessageResponse("Not found"));
	}

	// Fallback: log-free generic message so internals never leak.
	@ExceptionHandler(Exception.class)
	public ResponseEntity<MessageResponse> handleUnexpected(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new MessageResponse("Unexpected server error"));
	}
}
