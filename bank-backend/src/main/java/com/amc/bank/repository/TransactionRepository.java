package com.amc.bank.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.amc.bank.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

	/**
	 * Transaction history for one account, newest first. Paged so history is
	 * never loaded into memory unbounded.
	 */
	@Query("select t from Transaction t where t.account.id = :accountId "
			+ "order by t.timestamp desc, t.id desc")
	Page<Transaction> findByAccountIdOrderByTimestampDesc(
			@Param("accountId") Long accountId, Pageable pageable);
}
