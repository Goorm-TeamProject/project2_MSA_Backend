package com.eouil.msa.money.transaction.services;

import com.eouil.msa.money.account.domains.Account;
import com.eouil.msa.money.account.repositories.AccountRepository;
import com.eouil.msa.money.account.services.AccountService;
import com.eouil.msa.money.transaction.domains.Transaction;
import com.eouil.msa.money.transaction.domains.TransactionStatus;
import com.eouil.msa.money.transaction.domains.TransactionType;
import com.eouil.msa.money.transaction.dtos.DepositRequestDTO;
import com.eouil.msa.money.transaction.dtos.TransferRequestDTO;
import com.eouil.msa.money.transaction.dtos.TransferResponseDTO;
import com.eouil.msa.money.transaction.dtos.WithdrawRequestDTO;
import com.eouil.msa.money.transaction.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public List<TransferResponseDTO> transfer(TransferRequestDTO req, String userId) {
        Account from = handleWithdraw(req.getFromAccountNumber(), req.getAmount());
        Account to = handleDeposit(req.getToAccountNumber(), req.getAmount());
        Transaction tx = createTransferTransaction(from, to, req);
        return buildTransferResponses(tx, from, to);
    }

    @Transactional
    public TransferResponseDTO withdraw(WithdrawRequestDTO request, String userId) {
        Account from = handleWithdraw(request.getFromAccountNumber(), request.getAmount());
        Transaction tx = createSimpleTransaction(from, null, TransactionType.WITHDRAWAL, request.getAmount(), request.getMemo(), from.getBalance());
        return buildResponse(tx);
    }

    @Transactional
    public TransferResponseDTO deposit(DepositRequestDTO request, String userId) {
        Account to = handleDeposit(request.getToAccountNumber(), request.getAmount());
        Transaction tx = createSimpleTransaction(null, to, TransactionType.DEPOSIT, request.getAmount(), request.getMemo(), to.getBalance());
        return buildResponse(tx);
    }

    public List<TransferResponseDTO> getTransactions(String userId) {
        log.info("[GET TRANSACTIONS] 요청 - 사용자: {}", userId);

        Account account = accountRepository.findByUserId(userId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        String accountNumber = account.getAccountNumber();

        List<Transaction> transactions =
                transactionRepository.findByFromAccountIdOrToAccountIdOrderByCreatedAtAsc(accountNumber, accountNumber);

        log.info("[GET TRANSACTIONS] 완료 - 계좌: {}, 거래 수: {}", accountNumber, transactions.size());

        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    private Account handleWithdraw(String fromAccountNumber, BigDecimal amount) {
        return accountService.withdraw(fromAccountNumber, amount);
    }

    private Account handleDeposit(String toAccountNumber, BigDecimal amount) {
        return accountService.deposit(toAccountNumber, amount);
    }

    private Transaction createTransferTransaction(Account from, Account to, TransferRequestDTO req) {
        Transaction tx = Transaction.builder()
                .fromAccountId(from.getAccountNumber())
                .toAccountId(to.getAccountNumber())
                .type(TransactionType.TRANSFER)
                .amount(req.getAmount())
                .memo(req.getMemo())
                .status(TransactionStatus.COMPLETED)
                .balanceAfter(from.getBalance())
                .createdAt(LocalDateTime.now())
                .build();
        return transactionRepository.save(tx);
    }

    private Transaction createSimpleTransaction(Account from, Account to, TransactionType type, BigDecimal amount, String memo, BigDecimal balanceAfter) {
        Transaction tx = Transaction.builder()
                .fromAccountId(from != null ? from.getAccountNumber() : null)
                .toAccountId(to != null ? to.getAccountNumber() : null)
                .type(type)
                .amount(amount)
                .memo(memo)
                .status(TransactionStatus.COMPLETED)
                .balanceAfter(balanceAfter)
                .createdAt(LocalDateTime.now())
                .build();
        return transactionRepository.save(tx);
    }

    private TransferResponseDTO buildResponse(Transaction tx) {
        return TransferResponseDTO.builder()
                .transactionId(tx.getTransactionId())
                .fromAccountNumber(tx.getFromAccountId())
                .toAccountNumber(tx.getToAccountId())
                .type(tx.getType())
                .amount(tx.getAmount())
                .memo(tx.getMemo())
                .status(tx.getStatus().name())
                .balanceAfter(tx.getBalanceAfter())
                .createdAt(tx.getCreatedAt())
                .build();
    }

    private List<TransferResponseDTO> buildTransferResponses(Transaction tx, Account from, Account to) {
        TransferResponseDTO sent = TransferResponseDTO.builder()
                .transactionId(tx.getTransactionId())
                .fromAccountNumber(from.getAccountNumber())
                .toAccountNumber(to.getAccountNumber())
                .type(TransactionType.WITHDRAWAL)
                .amount(tx.getAmount().negate())
                .memo(tx.getMemo())
                .status(tx.getStatus().name())
                .balanceAfter(from.getBalance())
                .createdAt(tx.getCreatedAt())
                .build();

        TransferResponseDTO recv = TransferResponseDTO.builder()
                .transactionId(tx.getTransactionId())
                .fromAccountNumber(from.getAccountNumber())
                .toAccountNumber(to.getAccountNumber())
                .type(TransactionType.DEPOSIT)
                .amount(tx.getAmount())
                .memo(tx.getMemo())
                .status(tx.getStatus().name())
                .balanceAfter(to.getBalance())
                .createdAt(tx.getCreatedAt())
                .build();

        return List.of(sent, recv);
    }
}
