package com.amc.bank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amc.bank.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	List<Customer> findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(String name,
			String city);

	boolean existsByEmail(String email);

	boolean existsByPanNumber(String panNumber);
}
