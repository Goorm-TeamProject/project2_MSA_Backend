package com.eouil.msa.money.account.services;


import com.eouil.msa.money.account.domains.Account;
import com.eouil.msa.money.account.dtos.CreateAccountRequest;
import com.eouil.msa.money.account.dtos.CreateAccountResponse;
import com.eouil.msa.money.account.dtos.GetMyAccountResponse;
import com.eouil.msa.money.account.repositories.AccountRepository;
import com.eouil.msa.shared.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final JwtUtil jwtUtil;

    public AccountService(AccountRepository accountRepository, JwtUtil jwtUtil) {
        this.accountRepository = accountRepository;
        this.jwtUtil = jwtUtil;
    }

    public List<GetMyAccountResponse> getMyaccount(String token) {
        String userId = jwtUtil.validateTokenAndGetUserId(token);
        log.info("[GET MY ACCOUNT] 요청 - userId: {}", userId);

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
        log.info("[CREATE ACCOUNT] 요청 - userId: {}", userId);

        String accountNumber = generateUniqueAccountNumber();

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setUserId(userId);
        account.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        account.setCreatedAt(LocalDateTime.now());

        accountRepository.save(account);

        return new CreateAccountResponse(
                account.getAccountNumber(),
                userId,
                account.getBalance(),
                account.getCreatedAt()
        );
    }

    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
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

    private String generateUniqueAccountNumber() {
        String number;
        do {
            number = String.valueOf(10000000000000L + (long) (Math.random() * 89999999999999L));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }


}
