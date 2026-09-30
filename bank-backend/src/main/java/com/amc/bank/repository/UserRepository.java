package com.amc.bank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.amc.bank.model.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

	Optional<AppUser> findByUsernameIgnoreCase(String username);

	boolean existsByUsernameIgnoreCase(String username);

	// Explicit JPQL avoids ambiguity: AppUser stores Customer customer, not customerId.
	@Query("select u from AppUser u where u.customer.id = :customerId")
	Optional<AppUser> findByCustomerId(@Param("customerId") Long customerId);
}
