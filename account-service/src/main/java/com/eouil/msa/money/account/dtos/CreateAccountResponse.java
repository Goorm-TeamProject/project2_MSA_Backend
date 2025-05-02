package com.eouil.msa.money.account.dtos;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class CreateAccountResponse {
    private String accountNumber;
    private String userId;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public CreateAccountResponse(String accountNumber, String userId, BigDecimal balance, LocalDateTime createdAt) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.balance = balance;
        this.createdAt = createdAt;
    }
}
