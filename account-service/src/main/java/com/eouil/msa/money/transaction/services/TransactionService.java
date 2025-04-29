package com.eouil.msa.money.transaction.services;

import com.eouil.msa.money.account.client.AccountClient;
import com.eouil.msa.money.account.domains.Account;
import com.eouil.msa.money.transaction.domains.Transaction;
import com.eouil.msa.money.transaction.domains.TransactionStatus;
import com.eouil.msa.money.transaction.domains.TransactionType;
import com.eouil.msa.money.transaction.dtos.DepositRequestDTO;
import com.eouil.msa.money.transaction.dtos.TransferRequestDTO;
import com.eouil.msa.money.transaction.dtos.WithdrawRequestDTO;
import com.eouil.msa.money.transaction.repositories.TransactionJdbcRepository;
import com.eouil.msa.money.transaction.repositories.TransactionRepository;
import com.eouil.msa.money.transaction.dtos.TransferResponseDTO;
import com.eouil.msa.shared.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountClient accountClient;
    private final TransactionJdbcRepository transactionRepository;
    private final TransactionRepository transactionJPARepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public TransferResponseDTO buildResponse(TransferRequestDTO request, String token) {
        String userId = jwtUtil.validateTokenAndGetUserId(token);

        // 출금
        Account fromAccount = accountClient.withdraw(new WithdrawRequestDTO(request.getFromAccountNumber(), request.getAmount(), request.getMemo()));

        // 입금
        Account toAccount = accountClient.deposit(new DepositRequestDTO(request.getToAccountNumber(), request.getAmount(), request.getMemo()));

        // 거래 기록 저장 (이건 직접)
        Transaction tx = Transaction.builder()
                .fromAccountId(fromAccount.getAccountNumber())  // 출금 계좌의 accountNumber
                .toAccountId(toAccount.getAccountNumber())      // 입금 계좌의 accountNumber
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .memo(request.getMemo())
                .status(TransactionStatus.COMPLETED)
                .balanceAfter(fromAccount.getBalance())
                .createdAt(LocalDateTime.now())
                .build();


        transactionRepository.save(tx);

        return buildResponse(tx);
    }


    @Transactional
    public TransferResponseDTO withdraw(WithdrawRequestDTO request, String token) {
        String userId = jwtUtil.validateTokenAndGetUserId(token);

        Account fromAccount = accountClient.withdraw(request);

        Transaction tx = Transaction.builder()
                .fromAccountId(fromAccount.getAccountNumber())
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .memo(request.getMemo())
                .status(TransactionStatus.COMPLETED)
                .balanceAfter(fromAccount.getBalance())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(tx);
        return buildResponse(tx);
    }

    @Transactional
    public TransferResponseDTO deposit(DepositRequestDTO request, String token) {
        String userId = jwtUtil.validateTokenAndGetUserId(token);

        Account toAccount = accountClient.deposit(request);

        Transaction tx = Transaction.builder()
                .toAccountId(toAccount.getAccountNumber())
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .memo(request.getMemo())
                .status(TransactionStatus.COMPLETED)
                .balanceAfter(toAccount.getBalance())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(tx);
        return buildResponse(tx);
    }

    private TransferResponseDTO buildResponse(Transaction tx) {
        return TransferResponseDTO.builder()
                .transactionId(String.valueOf(tx.getTransactionId()))
                .fromAccountNumber(tx.getFromAccountId())
                .toAccountNumber(tx.getToAccountId())
                .type(tx.getType())
                .amount(tx.getAmount())
                .memo(tx.getMemo())
                .status(String.valueOf(tx.getStatus()))
                .balanceAfter(tx.getBalanceAfter())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
