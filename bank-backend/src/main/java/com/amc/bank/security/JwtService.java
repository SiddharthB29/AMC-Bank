package com.amc.bank.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Generates and validates signed JWTs (HS256) for both admin and customer logins.
 *
 * <p>The secret comes exclusively from the {@code jwt.secret} property, which is
 * supplied through the {@code AMC_BANK_JWT_SECRET} environment variable. There is
 * deliberately no fallback value: startup fails if it is not configured. The
 * secret is never logged.</p>
 */
@Service
public class JwtService {

	private final SecretKey key;
	private final long expirationMs;

	public JwtService(@Value("${jwt.secret}") String secret,
			@Value("${jwt.expiration-ms}") long expirationMs) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	/**
	 * Builds a token whose subject is the login username and whose {@code role}
	 * claim carries the ROLE_ADMIN/ROLE_CUSTOMER authority.
	 */
	public String generateToken(String username, String role) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(username)
				.claim("role", role)
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key)
				.compact();
	}

	/**
	 * Verifies the signature and expiry, returning the claims.
	 * Throws a JwtException on invalid or expired tokens.
	 */
	public Claims parseToken(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
