package com.amc.bank.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.amc.bank.dto.LoginRequest;
import com.amc.bank.dto.LoginResponse;
import com.amc.bank.model.AppUser;
import com.amc.bank.repository.UserRepository;
import com.amc.bank.security.JwtService;

/**
 * Authentication support: verifies credentials for the public login endpoint,
 * issues signed JWTs, and resolves users by login identity.
 *
 * <p>Failed logins return an empty result; no password material is ever logged.</p>
 */
@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	/**
	 * Verifies the credentials for both admin ("admin") and customer logins
	 * (username = customer's email). Case-insensitive username lookup, BCrypt
	 * password check. Returns the user unchanged on success or empty on
	 * failure; never distinguishes "unknown user" from "wrong password".
	 */
	public Optional<AppUser> verifyCredentials(String username, String password) {
		String candidate = username == null ? "" : username.trim();
		if (candidate.isEmpty() || password == null) {
			return Optional.empty();
		}
		Optional<AppUser> user = userRepository.findByUsernameIgnoreCase(candidate);
		if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPassword())) {
			return Optional.empty();
		}
		return user;
	}

	/**
	 * Full login flow: verify credentials, normalize any legacy role value the
	 * same way the token filter does, and issue a signed JWT carrying the
	 * identity and role claims.
	 */
	public Optional<LoginResponse> authenticate(LoginRequest request) {
		return verifyCredentials(request.getUsername(), request.getPassword())
				.map(user -> {
					String role = normalizeRole(user.getRole());
					if (!role.equals(user.getRole())) {
						user.setRole(role);
						userRepository.save(user);
					}
					String token = jwtService.generateToken(user.getUsername(), role);
					return new LoginResponse(token, user.getUsername(), role);
				});
	}

	/**
	 * Normalizes legacy role values such as "ADMIN" or "admin" into
	 * "ROLE_ADMIN" (and blank into ROLE_CUSTOMER), matching the reference's
	 * login flow and token filter.
	 */
	public String normalizeRole(String role) {
		if (role == null || role.isBlank()) {
			return "ROLE_CUSTOMER";
		}
		String normalized = role.trim().toUpperCase(Locale.ROOT);
		return normalized.startsWith("ROLE_")
				? normalized
				: "ROLE_" + normalized;
	}
}
