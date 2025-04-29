package com.eouil.msa.money.account.client;

import com.eouil.msa.money.account.domains.Account;
import com.eouil.msa.money.transaction.dtos.DepositRequestDTO;
import com.eouil.msa.money.transaction.dtos.WithdrawRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@FeignClient(name = "account-service", url = "http://account-service:8081")
public interface AccountClient {

    @PostMapping("/accounts/withdraw")
    Account withdraw(@RequestBody WithdrawRequestDTO request);

    @PostMapping("/accounts/deposit")
    Account deposit(@RequestBody DepositRequestDTO request);

    @GetMapping("/accounts/my")
    List<Account> getMyAccounts(@RequestHeader("Authorization") String token);
}

