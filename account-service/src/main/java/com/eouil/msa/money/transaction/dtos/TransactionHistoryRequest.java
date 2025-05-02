package com.eouil.msa.money.transaction.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryRequest {

    private String accountNumber;
}
