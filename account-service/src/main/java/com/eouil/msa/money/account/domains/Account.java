package com.eouil.msa.money.account.domains;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String accountId;

    private String accountNumber;

    private String userId; // 직접 userId 필드만 저장

    private BigDecimal balance;

    private LocalDateTime createdAt;

    @Builder
    public Account(String accountNumber, String userId, BigDecimal balance, LocalDateTime createdAt) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.balance = balance;
        this.createdAt = createdAt;
    }
}