package com.eouil.msa.money.transaction.dtos;

import com.eouil.msa.money.transaction.domains.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponseDTO {

    private String transactionId;        // 트랜잭션 고유 ID
    private String fromAccountNumber;    // 출금 계좌 번호
    private String toAccountNumber;      // 입금 계좌 번호
    private BigDecimal amount;           // 송금 금액
    private String memo;                 // 메모 (선택)
    private String status;               // 거래 상태 (예: COMPLETED, FAILED)
    private BigDecimal balanceAfter;     // 송금 후 출금계좌 잔액
    private LocalDateTime createdAt;
    private TransactionType type; // 거래 발생 시간
}
