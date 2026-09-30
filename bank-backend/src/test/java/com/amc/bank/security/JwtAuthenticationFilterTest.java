package com.amc.bank.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.amc.bank.model.AppUser;
import com.amc.bank.repository.UserRepository;

import jakarta.servlet.FilterChain;

/** Unit tests for the bearer-token filter using a real JwtService. */
class JwtAuthenticationFilterTest {

	private UserRepository userRepository;
	private JwtService jwtService;
	private JwtAuthenticationFilter filter;
	private FilterChain chain;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		byte[] secret = new byte[48];
		new java.security.SecureRandom().nextBytes(secret);
		jwtService = new JwtService(
				java.util.Base64.getEncoder().encodeToString(secret), 60_000);
		filter = new JwtAuthenticationFilter(jwtService, userRepository);
		chain = mock(FilterChain.class);
		SecurityContextHolder.clearContext();
	}

	private MockHttpServletRequest requestWithToken(String token) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("Authorization", "Bearer " + token);
		return request;
	}

	@Test
	void setsAuthenticationForValidBearerToken() throws Exception {
		String token = jwtService.generateToken("admin", "ROLE_ADMIN");
		AppUser user = new AppUser("admin", "$2a$hash", "ROLE_ADMIN", null);
		when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));

		filter.doFilterInternal(requestWithToken(token),
				new MockHttpServletResponse(), chain);

		var authentication = SecurityContextHolder.getContext().getAuthentication();
		assertEquals("admin", authentication.getName());
		assertEquals(java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
				authentication.getAuthorities());
		assertEquals("admin", authentication.getPrincipal());
		assertNull(authentication.getCredentials());
		verify(chain).doFilter(any(jakarta.servlet.http.HttpServletRequest.class),
				any(jakarta.servlet.http.HttpServletResponse.class));
	}

	@Test
	void normalizesLegacyRoleFromDatabase() throws Exception {
		String token = jwtService.generateToken("admin", "ROLE_ADMIN");
		// Legacy database value without the ROLE_ prefix.
		AppUser user = new AppUser("admin", "$2a$hash", "ADMIN", null);
		when(userRepository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));

		filter.doFilterInternal(requestWithToken(token),
				new MockHttpServletResponse(), chain);

		var authentication = SecurityContextHolder.getContext().getAuthentication();
		assertEquals(java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
				authentication.getAuthorities());
	}

	@Test
	void invalidTokenLeavesRequestUnauthenticatedButContinues() throws Exception {
		filter.doFilterInternal(requestWithToken("not-a-jwt"),
				new MockHttpServletResponse(), chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}

	@Test
	void missingOrNonBearerHeaderLeavesRequestUnauthenticated() throws Exception {
		MockHttpServletRequest noHeader = new MockHttpServletRequest();
		filter.doFilterInternal(noHeader, new MockHttpServletResponse(), chain);

		MockHttpServletRequest wrongScheme = new MockHttpServletRequest();
		wrongScheme.addHeader("Authorization", "Basic dXNlcjpwYXNz");
		filter.doFilterInternal(wrongScheme, new MockHttpServletResponse(), chain);

		MockHttpServletRequest emptyBearer = new MockHttpServletRequest();
		emptyBearer.addHeader("Authorization", "Bearer ");
		filter.doFilterInternal(emptyBearer, new MockHttpServletResponse(), chain);

		assertNull(SecurityContextHolder.getContext().getAuthentication());
	}
}
