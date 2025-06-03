package com.eouil.msa.money.transaction.domains;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "transaction")
public class Transaction {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "transaction_id", length = 36, updatable = false, nullable = false)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column
    private BigDecimal balanceAfter;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 추가해야 할 부분
    @Column(nullable = true)
    private String fromAccountId;

    @Column(nullable = true)
    private String toAccountId;

    // 기본 생성자
    protected Transaction() {}

    @Builder
    public Transaction(String fromAccountId, String toAccountId, TransactionType type, BigDecimal amount,
                       String memo, TransactionStatus status, BigDecimal balanceAfter, LocalDateTime createdAt) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.type = type;
        this.amount = amount;
        this.memo = memo;
        this.status = status != null ? status : TransactionStatus.PENDING;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
