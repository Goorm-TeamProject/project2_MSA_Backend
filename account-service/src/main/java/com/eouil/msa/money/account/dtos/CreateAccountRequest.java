package com.eouil.msa.money.account.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter@Setter
public class CreateAccountRequest {

    @NotNull(message = "잔액은 필수 항목입니다.")
    @Positive(message = "잔액은 양수여야 합니다.")
    private BigDecimal balance;
}
