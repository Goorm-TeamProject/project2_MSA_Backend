package com.eouil.msa.money.transaction.controllers;

import com.eouil.msa.money.transaction.dtos.*;
import com.eouil.msa.money.transaction.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<List<TransferResponseDTO>> transfer(
            @RequestBody TransferRequestDTO req,
            @RequestHeader("Authorization") String token
    ) {
        List<TransferResponseDTO> result = transactionService.transfer(req, token);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransferResponseDTO> withdraw(
            @RequestBody WithdrawRequestDTO request,
            @RequestHeader("Authorization") String token
    ) {
        log.info("[POST /withdraw] 요청 도착: {}", request);
        TransferResponseDTO response = transactionService.withdraw(request, token);
        log.info("[POST /withdraw] 처리 완료: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransferResponseDTO> deposit(
            @RequestBody DepositRequestDTO request,
            @RequestHeader("Authorization") String token
    ) {
        log.info("[POST /deposit] 요청 도착: {}", request);
        TransferResponseDTO response = transactionService.deposit(request, token);
        log.info("[POST /deposit] 처리 완료: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransferResponseDTO>> getTransactions(
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String token = authorizationHeader.replace("Bearer ", "");
        log.info("[GET /transactions] 요청 도착 (토큰: {})", token.substring(0, Math.min(token.length(), 10)) + "...");
        List<TransferResponseDTO> transactions = transactionService.getTransactions(token);
        log.info("[GET /transactions] 조회 완료: {}건", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("ALB Transactions-service Health Check OK");
    }
}
