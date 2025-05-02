package com.eouil.msa.money.transaction.services;

import com.eouil.msa.money.account.domains.Account;
import com.eouil.msa.money.account.repositories.AccountRepository;
import com.eouil.msa.money.account.services.AccountService;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountService accountService;
    private final TransactionJdbcRepository transactionRepository;
    private final TransactionRepository transactionJPARepository;
    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;

    @Transactional
    public List<TransferResponseDTO> transfer(TransferRequestDTO req, String token) {
        String userId = jwtUtil.validateTokenAndGetUserId(token);

        // 출금/입금 처리
        Account from = accountService.withdraw(req.getFromAccountNumber(), req.getAmount());
        Account to   = accountService.deposit( req.getToAccountNumber(),  req.getAmount());

        // DB 저장
        Transaction tx = Transaction.builder()
                .fromAccountId(from.getAccountNumber())
                .toAccountId(  to.getAccountNumber())
                .type(         TransactionType.TRANSFER)
                .amount(       req.getAmount())
                .memo(         req.getMemo())
                .status(       TransactionStatus.COMPLETED)
                .balanceAfter( from.getBalance())
                .createdAt(    LocalDateTime.now())
                .build();
        transactionRepository.save(tx);

        // – 보낸(출금) 레코드
        TransferResponseDTO sentDto = TransferResponseDTO.builder()
                .transactionId(   tx.getTransactionId())
                .fromAccountNumber(tx.getFromAccountId())
                .toAccountNumber(  tx.getToAccountId())
                .type(             TransactionType.WITHDRAWAL)
                .amount(           tx.getAmount().negate())
                .memo(             tx.getMemo())
                .status(           tx.getStatus().name())
                .balanceAfter(     tx.getBalanceAfter())
                .createdAt(        tx.getCreatedAt())
                .build();

        // + 받은(입금) 레코드
        TransferResponseDTO recvDto = TransferResponseDTO.builder()
                .transactionId(   tx.getTransactionId())
                .fromAccountNumber(tx.getFromAccountId())
                .toAccountNumber(  tx.getToAccountId())
                .type(             TransactionType.DEPOSIT)
                .amount(           tx.getAmount())
                .memo(             tx.getMemo())
                .status(           tx.getStatus().name())
                .balanceAfter(     to.getBalance())
                .createdAt(        tx.getCreatedAt())
                .build();

        return List.of(sentDto, recvDto);
    }


    @Transactional
    public TransferResponseDTO withdraw(WithdrawRequestDTO request, String token) {
        String userId = jwtUtil.validateTokenAndGetUserId(token);

        Account from = accountService.withdraw(request.getFromAccountNumber(), request.getAmount());

        Transaction tx = Transaction.builder()
                .fromAccountId(from.getAccountNumber())
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .memo(request.getMemo())
                .status(TransactionStatus.COMPLETED)
                .balanceAfter(from.getBalance())
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);
        return buildResponse(tx);
    }

    @Transactional
    public TransferResponseDTO deposit(DepositRequestDTO request, String token) {
        String userId = jwtUtil.validateTokenAndGetUserId(token);

        Account to = accountService.deposit(request.getToAccountNumber(), request.getAmount());

        Transaction tx = Transaction.builder()
                .toAccountId(to.getAccountNumber())
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .memo(request.getMemo())
                .status(TransactionStatus.COMPLETED)
                .balanceAfter(to.getBalance())
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);
        return buildResponse(tx);
    }

    public List<TransferResponseDTO> getTransactions(String token) {
        String userId = jwtUtil.validateTokenAndGetUserId(token);
        log.info("[GET TRANSACTIONS] 요청 - 사용자: {}", userId);

        // 해당 userId의 단일 계좌 조회
        Account account = accountRepository.findByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        String accountNumber = account.getAccountNumber();

        // 2. 해당 계좌의 모든 거래 내역 조회
        List<Transaction> transactions =
                transactionJPARepository.findByFromAccountIdOrToAccountId(accountNumber, accountNumber);

        log.info("[GET TRANSACTIONS] 완료 - 계좌: {}, 거래 수: {}", accountNumber, transactions.size());

        // DTO로 변환 및 시간순 정렬
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .map(this::buildResponse)
                .collect(Collectors.toList());
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
