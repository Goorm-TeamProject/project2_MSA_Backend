package com.eouil.msa.money.transaction.repositories;

import com.eouil.msa.money.transaction.domains.Transaction;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class TransactionJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void save(Transaction tx) {
        String sql = "INSERT INTO transaction (from_account_id, to_account_id, type, amount, memo, status, balance_after, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                tx.getFromAccountId(),
                tx.getToAccountId(),
                tx.getType().name(),
                tx.getAmount(),
                tx.getMemo(),
                tx.getStatus().name(),
                tx.getBalanceAfter(),
                LocalDateTime.now()
        );
    }
}
