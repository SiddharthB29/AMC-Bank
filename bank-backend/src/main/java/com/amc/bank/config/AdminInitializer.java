package com.amc.bank.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.amc.bank.model.AppUser;
import com.amc.bank.repository.UserRepository;
import com.amc.bank.service.AuthService;

/**
 * Startup bootstrap ported from the reference application:
 *
 * <ul>
 *   <li>repairs legacy role values (e.g. ADMIN to ROLE_ADMIN) when an older
 *       database is reused, and</li>
 *   <li>creates the demo admin login (username "admin") the first time the
 *       application runs; an existing admin keeps its password.</li>
 * </ul>
 *
 * <p>The initial admin password defaults to the reference project's published
 * classroom default and can be overridden through the AMC_BANK_ADMIN_PASSWORD
 * environment variable. It is BCrypt-hashed before storage and never logged.</p>
 */
@Configuration
public class AdminInitializer {

	@Bean
	CommandLineRunner initializeUsers(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AuthService authService) {
		return args -> {
			// Repair legacy role values so Spring Security consistently sees
			// ROLE_ADMIN or ROLE_CUSTOMER.
			userRepository.findAll().forEach(user -> {
				String normalized = authService.normalizeRole(user.getRole());
				if (!normalized.equals(user.getRole())) {
					user.setRole(normalized);
					userRepository.save(user);
				}
			});
			AppUser admin = userRepository.findByUsernameIgnoreCase("admin")
					.orElse(null);
			if (admin == null) {
				String initialPassword = System.getenv().getOrDefault(
						"AMC_BANK_ADMIN_PASSWORD", "admin123");
				userRepository.save(new AppUser(
						"admin",
						passwordEncoder.encode(initialPassword),
						"ROLE_ADMIN"));
			} else if (!"ROLE_ADMIN".equals(admin.getRole())) {
				admin.setRole("ROLE_ADMIN");
				userRepository.save(admin);
			}
		};
	}
}
