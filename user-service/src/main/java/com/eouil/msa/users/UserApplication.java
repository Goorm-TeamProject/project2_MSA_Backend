package com.eouil.msa.users;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.eouil.msa.users",
        "com.eouil.msa.shared"
})
public class UserApplication {
    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.load();
            System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        } catch (Exception e) {
            // .env 파일 없으면 무시하고 진행 (K8s 환경에서는 환경변수로 주입)
            System.out.println(".env 파일이 없어 dotenv 로드 생략");
        }
        SpringApplication.run(UserApplication.class, args);
    }
}
