package com.eouil.msa.money.account.repositories;

import com.eouil.msa.money.account.domains.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    //계좌 존재 확인
    boolean existsByAccountNumber(String accountNumber);

    // 계좌 찾기
    Optional<Account> findByAccountNumber(String accountNumber);

    //유저 찾기
    List<Account> findByUserId(String userId);

    @Query(value = "SELECT * FROM account WHERE account_number = :accountNumber FOR UPDATE", nativeQuery = true)
    Account findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}
