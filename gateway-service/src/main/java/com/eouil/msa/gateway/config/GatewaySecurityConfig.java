package com.eouil.msa.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
@Slf4j
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.disable())  // CORS는 게이트웨이 설정으로 처리
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(ex -> ex
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers("/api/users/join",
                                "/api/users/login",
                                "/api/users/refresh",
                                "/api/users/mfa/**").permitAll()
                        .anyExchange().permitAll()
                )
                .build();
    }
}