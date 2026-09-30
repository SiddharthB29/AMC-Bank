package com.amc.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.amc.bank.dto.LoginRequest;
import com.amc.bank.dto.LoginResponse;
import com.amc.bank.model.AppUser;
import com.amc.bank.repository.UserRepository;
import com.amc.bank.security.JwtService;

class AuthServiceTest {

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private JwtService jwtService;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);
		jwtService = mock(JwtService.class);
		authService = new AuthService(userRepository, passwordEncoder, jwtService);
	}

	@Test
	void verifyCredentialsAcceptsValidUser() {
		AppUser user = new AppUser("admin", "$2a$hash", "ROLE_ADMIN", null);
		when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("admin123", "$2a$hash")).thenReturn(true);

		Optional<AppUser> result = authService.verifyCredentials("admin", "admin123");

		assertTrue(result.isPresent());
		assertEquals("ROLE_ADMIN", result.get().getRole());
	}

	@Test
	void verifyCredentialsRejectsWrongPassword() {
		AppUser user = new AppUser("admin", "$2a$hash", "ROLE_ADMIN", null);
		when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("nope", "$2a$hash")).thenReturn(false);

		assertTrue(authService.verifyCredentials("admin", "nope").isEmpty());
	}

	@Test
	void verifyCredentialsRejectsUnknownUserAndBlankInput() {
		when(userRepository.findByUsernameIgnoreCase(anyString())).thenReturn(Optional.empty());
		assertTrue(authService.verifyCredentials("ghost", "x").isEmpty());
		assertTrue(authService.verifyCredentials("  ", "x").isEmpty());
		assertTrue(authService.verifyCredentials(null, "x").isEmpty());
		assertTrue(authService.verifyCredentials("admin", null).isEmpty());
	}

	@Test
	void verifyCredentialsIsCaseInsensitiveOnUsername() {
		AppUser user = new AppUser("admin", "$2a$hash", "ROLE_ADMIN", null);
		when(userRepository.findByUsernameIgnoreCase("ADMIN")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

		assertTrue(authService.verifyCredentials("ADMIN", "admin123").isPresent());
	}

	@Test
	void normalizeRoleMatchesReferenceBehavior() {
		assertEquals("ROLE_ADMIN", authService.normalizeRole("ADMIN"));
		assertEquals("ROLE_ADMIN", authService.normalizeRole("admin"));
		assertEquals("ROLE_ADMIN", authService.normalizeRole(" ROLE_ADMIN "));
		assertEquals("ROLE_CUSTOMER", authService.normalizeRole("CUSTOMER"));
		assertEquals("ROLE_CUSTOMER", authService.normalizeRole(""));
		assertEquals("ROLE_CUSTOMER", authService.normalizeRole(null));
	}

	@Test
	void authenticateIssuesTokenWithNormalizedRole() {
		AppUser user = new AppUser("pritam@example.com", "$2a$hash", "CUSTOMER", null);
		when(userRepository.findByUsernameIgnoreCase("pritam@example.com"))
				.thenReturn(Optional.of(user));
		when(passwordEncoder.matches("secret", "$2a$hash")).thenReturn(true);
		when(jwtService.generateToken("pritam@example.com", "ROLE_CUSTOMER"))
				.thenReturn("jwt-token");

		LoginRequest request = new LoginRequest();
		request.setUsername("pritam@example.com");
		request.setPassword("secret");

		Optional<LoginResponse> response = authService.authenticate(request);

		assertTrue(response.isPresent());
		assertEquals("jwt-token", response.get().getToken());
		assertEquals("pritam@example.com", response.get().getUsername());
		assertEquals("ROLE_CUSTOMER", response.get().getRole());
		// The legacy role value must have been persisted in normalized form.
		verify(userRepository).save(user);
	}

	@Test
	void authenticateFailsForBadCredentialsWithoutIssuingToken() {
		when(userRepository.findByUsernameIgnoreCase(anyString()))
				.thenReturn(Optional.empty());

		LoginRequest request = new LoginRequest();
		request.setUsername("ghost");
		request.setPassword("wrong");

		assertTrue(authService.authenticate(request).isEmpty());
		verify(userRepository, never()).save(new AppUser());
		verify(jwtService, never()).generateToken(anyString(), anyString());
	}
}
