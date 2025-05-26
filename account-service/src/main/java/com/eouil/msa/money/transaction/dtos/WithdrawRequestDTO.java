package com.eouil.msa.money.transaction.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class WithdrawRequestDTO {

    @NotBlank(message = "출금 계좌번호는 필수입니다.")
    private String fromAccountNumber;

    @NotNull(message = "금액은 필수입니다.")
    @DecimalMin(value = "0.01", message = "최소 출금 금액은 0.01입니다.")
    private BigDecimal amount;

    private String memo;
}