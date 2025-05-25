package com.eouil.msa.money.account.controllers;

import com.eouil.msa.money.account.dtos.CreateAccountRequest;
import com.eouil.msa.money.account.dtos.CreateAccountResponse;
import com.eouil.msa.money.account.dtos.GetMyAccountResponse;
import com.eouil.msa.money.account.services.AccountService;
import com.eouil.msa.shared.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<List<GetMyAccountResponse>> getMyAccounts(
            @AuthenticationPrincipal String userId  // CustomUserPrincipal → String
    ) {
        return ResponseEntity.ok(accountService.getMyaccount(userId));
    }

    @PostMapping
    public ResponseEntity<CreateAccountResponse> create(
            @AuthenticationPrincipal String userId,  // CustomUserPrincipal → String
            @RequestBody CreateAccountRequest request
    ) {
        return ResponseEntity.ok(accountService.createAccount(request, userId));
    }
}