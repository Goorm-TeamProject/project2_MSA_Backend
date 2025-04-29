package com.eouil.msa.money.transaction.repositories;

import com.eouil.msa.money.transaction.domains.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t " +
            "WHERE t.fromAccountId = :accountNumber " +
            "OR t.toAccountId = :accountNumber")
    List<Transaction> findByAccountNumber(@Param("accountNumber") String accountNumber);
}
