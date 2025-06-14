package com.eouil.msa.money;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.eouil.msa.money",
        "com.eouil.msa.shared",
        "com.eouil.msa.money.account"
})
@EnableFeignClients(basePackages = "com.eouil.msa.money.account")
public class AccountTransactionApplication {
    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.load();
            System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        } catch (Exception e) {
            System.out.println(".env 파일이 없어 dotenv 로드 생략");
        }
        SpringApplication.run(AccountTransactionApplication.class, args);
    }
}
