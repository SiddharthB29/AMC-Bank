package com.amc.bank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.amc.bank.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

	// Account stores a Customer object rather than a customerId column, so the
	// query explicitly navigates customer.id.
	@Query("select a from Account a where a.customer.id = :customerId")
	List<Account> findByCustomerId(@Param("customerId") Long customerId);

	long countByStatus(String status);
}
