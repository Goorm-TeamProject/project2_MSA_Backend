package com.eouil.msa.money.transaction.repositories;

import com.eouil.msa.money.transaction.domains.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByFromAccountIdOrToAccountIdOrderByCreatedAtAsc(
            String fromAccountId,
            String toAccountId
    );
}
