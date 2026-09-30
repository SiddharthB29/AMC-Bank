package com.amc.bank.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.SecureRandom;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

/**
 * Unit tests for token generation and validation. Each test uses a fresh
 * random test-only secret; the production secret is provided exclusively
 * through AMC_BANK_JWT_SECRET and is never referenced here.
 */
class JwtServiceTest {

	private JwtService newService(long ttlMs) {
		byte[] bytes = new byte[48];
		new SecureRandom().nextBytes(bytes);
		String secret = Base64.getEncoder().encodeToString(bytes);
		return new JwtService(secret, ttlMs);
	}

	@Test
	void tokenRoundTripCarriesIdentityAndRole() {
		JwtService service = newService(60_000);
		String token = service.generateToken("admin", "ROLE_ADMIN");

		var claims = service.parseToken(token);

		assertEquals("admin", claims.getSubject());
		assertEquals("ROLE_ADMIN", claims.get("role", String.class));
	}

	@Test
	void tokensDifferForDifferentIdentitiesAndRoles() {
		JwtService service = newService(60_000);
		String adminToken = service.generateToken("admin", "ROLE_ADMIN");
		String customerToken = service.generateToken("p@example.com", "ROLE_CUSTOMER");
		assertNotEquals(adminToken, customerToken);
		assertEquals("p@example.com",
				service.parseToken(customerToken).getSubject());
		assertEquals("ROLE_CUSTOMER",
				service.parseToken(customerToken).get("role", String.class));
	}

	@Test
	void expiredTokenIsRejected() {
		JwtService service = newService(-1000);
		String token = service.generateToken("admin", "ROLE_ADMIN");
		assertThrows(ExpiredJwtException.class, () -> service.parseToken(token));
	}

	@Test
	void tamperedTokenIsRejected() {
		JwtService service = newService(60_000);
		String token = service.generateToken("admin", "ROLE_ADMIN");
		String tampered = token.substring(0, token.length() - 2) + "xx";
		assertThrows(JwtException.class, () -> service.parseToken(tampered));
	}

	@Test
	void tokenSignedByAnotherKeyIsRejected() {
		JwtService issuer = newService(60_000);
		JwtService verifier = newService(60_000);
		String token = issuer.generateToken("admin", "ROLE_ADMIN");
		assertThrows(JwtException.class, () -> verifier.parseToken(token));
	}
}
