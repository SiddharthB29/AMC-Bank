package com.amc.bank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.amc.bank.model.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {

	// Explicit JPQL: Loan has a Customer field named "customer", so the query
	// navigates customer.id rather than relying on name-derived property paths.
	@Query("select l from Loan l where l.customer.id = :customerId")
	List<Loan> findByCustomerId(@Param("customerId") Long customerId);

	@Query("select count(l) from Loan l "
			+ "where l.customer.id = :customerId and l.status = :status")
	long countByCustomerIdAndStatus(@Param("customerId") Long customerId,
			@Param("status") String status);

	long countByStatus(String status);
}
