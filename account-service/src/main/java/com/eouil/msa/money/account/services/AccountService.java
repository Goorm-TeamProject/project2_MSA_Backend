package com.eouil.msa.money.account.services;


import com.eouil.msa.money.account.domains.Account;
import com.eouil.msa.money.account.dtos.CreateAccountRequest;
import com.eouil.msa.money.account.dtos.CreateAccountResponse;
import com.eouil.msa.money.account.dtos.GetMyAccountResponse;
import com.eouil.msa.money.account.repositories.AccountRepository;
import com.eouil.msa.shared.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public List<GetMyAccountResponse> getMyaccount(String userId) {
        log.info("[GET MY ACCOUNT] userId: {}", userId);

        List<Account> accountList = accountRepository.findByUserId(userId);

        return accountList.stream()
                .map(account -> new GetMyAccountResponse(
                        account.getAccountNumber(),
                        account.getBalance(),
                        account.getCreatedAt()
                ))
                .toList();
    }

    public CreateAccountResponse createAccount(CreateAccountRequest request, String userId) {
        log.info("[CREATE ACCOUNT] 요청 userId: {}", userId);

        String accountNumber = generateUniqueAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .userId(userId)
                .balance(Optional.ofNullable(request.getBalance()).orElse(BigDecimal.ZERO))
                .createdAt(LocalDateTime.now())
                .build();

        accountRepository.save(account);

        return new CreateAccountResponse(
                account.getAccountNumber(),
                userId,
                account.getBalance(),
                account.getCreatedAt()
        );
    }

    private String generateUniqueAccountNumber() {
        for (int i = 0; i < 10; i++) {
            String number = String.valueOf(10000000000000L + (long) (Math.random() * 89999999999999L));
            if (!accountRepository.existsByAccountNumber(number)) return number;
        }
        throw new IllegalStateException("유효한 계좌번호를 생성할 수 없습니다.");
    }

    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("잔액 부족");
        }
        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
    }

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }
}