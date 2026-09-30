package com.amc.bank.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Stateless JWT security: only /api/login is public, /api/customer/** is
 * customer-only, all other /api/** endpoints are admin-only, and /api/auth/me
 * is available to any authenticated user.
 *
 * <p>The BCrypt PasswordEncoder bean lives in
 * {@link com.amc.bank.config.PasswordConfig}.</p>
 */
@Configuration
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtFilter;

	public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
		this.jwtFilter = jwtFilter;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(
				"http://localhost:5173",
				"http://127.0.0.1:5173"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		UrlBasedCorsConfigurationSource source =
				new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(cors -> {})
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/api/login").permitAll()
						// Available to either authenticated role; the frontend uses
						// this to verify what role Spring Security actually sees.
						.requestMatchers("/api/auth/me").authenticated()
						// Customer self-service APIs.
						.requestMatchers("/api/customer/**")
						.hasAuthority("ROLE_CUSTOMER")
						// Administration APIs, including POST /api/customers.
						.requestMatchers("/api/**")
						.hasAuthority("ROLE_ADMIN")
						.anyRequest().authenticated())
				.exceptionHandling(errors -> errors
						.authenticationEntryPoint((request, response, ex) -> {
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							response.getWriter().write(
									"{\"message\":\"Unauthorized - please login again\"}");
						})
						.accessDeniedHandler((request, response, ex) -> {
							response.setStatus(HttpServletResponse.SC_FORBIDDEN);
							response.setContentType(MediaType.APPLICATION_JSON_VALUE);
							response.getWriter().write(
									"{\"message\":\"Forbidden - this login does not have the required role\"}");
						}))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}
}
