package com.amc.bank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amc.bank.dto.AuthMeResponse;
import com.amc.bank.dto.LoginRequest;
import com.amc.bank.dto.LoginResponse;
import com.amc.bank.dto.MessageResponse;
import com.amc.bank.service.AuthService;

/**
 * Public login and authenticated identity endpoints.
 *
 * <p>Failed logins return HTTP 401 with the reference's message, never
 * revealing whether the username or the password was wrong. Passwords and
 * JWT secrets are never logged.</p>
 */
@RestController
@RequestMapping("/api")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	// Public login for both admin and customer.
	// Admin username: admin. Customer username: the customer's email address.
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		var authentication = authService.authenticate(request);
		if (authentication.isPresent()) {
			return ResponseEntity.ok(authentication.get());
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new MessageResponse("Invalid username/email or password"));
	}

	// Lets the frontend confirm the identity/authority established by the JWT filter.
	@GetMapping("/auth/me")
	public AuthMeResponse authenticatedUser(Authentication authentication) {
		return new AuthMeResponse(
				authentication.getName(),
				authentication.getAuthorities().stream()
						.map(a -> a.getAuthority())
						.toList());
	}
}
