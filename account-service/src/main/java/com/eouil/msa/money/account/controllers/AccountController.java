package com.eouil.msa.money.account.controllers;

import com.eouil.msa.money.account.dtos.CreateAccountRequest;
import com.eouil.msa.money.account.dtos.CreateAccountResponse;
import com.eouil.msa.money.account.dtos.GetMyAccountResponse;
import com.eouil.msa.money.account.services.AccountService;
import com.eouil.msa.shared.jwt.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    public AccountController(AccountService accountService, JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/")
    public ResponseEntity<Void> createAccount(@RequestBody CreateAccountRequest request) {
        String userId = jwtUtil.validateTokenAndGetUserId(getAccessTokenFromHeader());

        log.info("[POST /accounts] 계좌 생성 요청 - 사용자 ID: {}, 요청 정보: {}", userId, request);

        accountService.createAccount(request, userId);

        log.info("[POST /accounts] 계좌 생성 완료 ");

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<List<GetMyAccountResponse>> getMyAccount(
            @RequestHeader("Authorization") String token) {

        log.info("[GET /accounts/me] 내 계좌 목록 조회 요청 (토큰 일부: {}...)", token.substring(0, Math.min(10, token.length())));
        List<GetMyAccountResponse> responses = accountService.getMyaccount(token);
        log.info("[GET /accounts/me] 내 계좌 {}건 조회 완료", responses.size());

        return ResponseEntity.ok(responses);
    }

    private String getAccessTokenFromHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("RequestAttributes is null");
        }
        String authorizationHeader = attributes.getRequest().getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header is missing or invalid");
        }
        return authorizationHeader.substring(7); // "Bearer " 이후 토큰만 잘라서 반환
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("ALB Account-service Health Check OK");
    }

}
