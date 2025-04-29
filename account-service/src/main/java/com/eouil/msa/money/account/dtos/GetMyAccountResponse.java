package com.eouil.msa.money.account.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GetMyAccountResponse
{
    private String accountNumber;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}

