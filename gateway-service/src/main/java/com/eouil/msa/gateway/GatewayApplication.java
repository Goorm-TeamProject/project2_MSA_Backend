package com.eouil.msa.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// servlet 보안 자동설정은 빼고, reactive 보안만 쓰겠다는 뜻
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(
        scanBasePackages = {
                "com.eouil.msa.gateway",
                "com.eouil.msa.shared" // 여기에 jwt도 있으니까 조심
        },
        exclude = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@ComponentScan(
        basePackages = {
                "com.eouil.msa.gateway",
                "com.eouil.msa.shared"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.eouil\\.msa\\.shared\\.jwt\\.JwtAuthenticationFilter"
        )
)
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
