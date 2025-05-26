// 📁 money-service/src/main/java/com/eouil/msa/money/transaction/controllers/TransactionController.java
package com.eouil.msa.money.transaction.controllers;

import com.eouil.msa.money.transaction.dtos.*;
import com.eouil.msa.money.transaction.services.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<List<TransferResponseDTO>> transfer(
            @RequestBody TransferRequestDTO req,
            @AuthenticationPrincipal String userId
    ) {
        List<TransferResponseDTO> result = transactionService.transfer(req, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransferResponseDTO> withdraw(
            @RequestBody WithdrawRequestDTO request,
            @AuthenticationPrincipal String userId
    ) {
        log.info("[POST /withdraw] 요청 도착: {}", request);
        TransferResponseDTO response = transactionService.withdraw(request, userId);
        log.info("[POST /withdraw] 처리 완료: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransferResponseDTO> deposit(
            @RequestBody DepositRequestDTO request,
            @AuthenticationPrincipal String userId
    ) {
        log.info("[POST /deposit] 요청 도착: {}", request);
        TransferResponseDTO response = transactionService.deposit(request, userId);
        log.info("[POST /deposit] 처리 완료: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TransferResponseDTO>> getTransactions(
            @AuthenticationPrincipal String userId
    ) {
        log.info("[GET /transactions] 사용자 ID: {}", userId);
        List<TransferResponseDTO> transactions = transactionService.getTransactions(userId);
        log.info("[GET /transactions] 조회 완료: {}건", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("ALB Transactions-service Health Check OK");
    }
}
