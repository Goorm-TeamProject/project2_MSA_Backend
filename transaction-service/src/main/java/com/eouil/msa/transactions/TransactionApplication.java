package com.eouil.msa.transactions;

import com.eouil.msa.users.UserApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransactionApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
