package com.eouil.msa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/api/users/join",
                                "/api/users/login",
                                "/api/users/logout",
                                "/api/users/refresh",
                                "/api/users/health"
                        ).permitAll()
                        // 추가: downstream 인증은 Gateway가 아닌 서비스 레벨에서 처리
                        .pathMatchers(
                                "/api/accounts/**",
                                "/api/transactions/**"
                        ).permitAll()
                        .anyExchange().denyAll()
                )
                .build();
    }


}