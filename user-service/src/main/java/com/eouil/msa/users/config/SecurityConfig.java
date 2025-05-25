package com.eouil.msa.users.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("🔧 Configuring SecurityFilterChain");

        return http
                .csrf(csrf -> {
                    csrf.disable();
                    log.debug("✔️ CSRF disabled");
                })
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/api/users/join", "/api/users/login", "/api/users/refresh").permitAll()
                            .requestMatchers("/api/users/mfa/**").permitAll()
                            .anyRequest().authenticated();
                    log.debug("✔️ Authorization rules configured");
                })
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}