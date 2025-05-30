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
        Dotenv dotenv = Dotenv.load();
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        var context = SpringApplication.run(UserApplication.class, args);
        Environment env = context.getEnvironment();
        System.out.println("✅ Active profiles: " + String.join(", ", env.getActiveProfiles()));
    }
}
