package com.eouil.msa.money;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.eouil.msa.money",
        "com.eouil.msa.shared",
        "com.eouil.msa.money.account.client"
})
@EnableFeignClients(basePackages = "com.eouil.msa.money.account.client")
public class AccountTransactionApplication {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        SpringApplication.run(AccountTransactionApplication.class, args);
    }
}
