package com.eouil.msa.money.account.domains;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter@Setter
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "account_id", unique = true, length = 36, updatable = false, nullable = false)
    private String accountId;

    @Column(length = 20)
    private String accountNumber;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    private String userId; //fk
}
