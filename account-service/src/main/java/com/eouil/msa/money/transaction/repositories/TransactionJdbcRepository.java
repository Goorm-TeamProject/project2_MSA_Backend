package com.eouil.msa.money.transaction.repositories;

import com.eouil.msa.money.transaction.domains.Transaction;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TransactionJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Transaction save(Transaction tx) {
        // 1) UUID 미리 생성
        String id = UUID.randomUUID().toString();
        tx.setTransactionId(id);

        // 2) transaction_id 컬럼까지 포함해서 INSERT
        String sql = """
            INSERT INTO transaction
              (transaction_id,
               from_account_id,
               to_account_id,
               type,
               amount,
               memo,
               status,
               balance_after,
               created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(sql,
                id,
                tx.getFromAccountId(),
                tx.getToAccountId(),
                tx.getType().name(),
                tx.getAmount(),
                tx.getMemo(),
                tx.getStatus().name(),
                tx.getBalanceAfter(),
                Timestamp.valueOf(tx.getCreatedAt())
        );

        return tx;
    }
}

