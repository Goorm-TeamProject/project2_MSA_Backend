package com.eouil.msa.money.account.domains;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter@Setter
public class Account {
    @Id
    @Column(length = 36)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String accountId;

    @Column(length = 20)
    private String accountNumber;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    private String userId; //fk
}
