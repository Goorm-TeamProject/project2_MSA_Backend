package com.eouil.msa.money.transaction.dtos;

import com.eouil.msa.money.transaction.domains.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransferResponseDTO {

    private String transactionId;

    private String fromAccountNumber;

    private String toAccountNumber;

    private TransactionType type;

    private BigDecimal amount;

    private String memo;

    private String status;

    private BigDecimal balanceAfter;

    private LocalDateTime createdAt;
}