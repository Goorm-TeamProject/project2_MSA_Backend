package com.eouil.msa.gateway.config;

import com.eouil.msa.shared.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtUtilConfig {

    @Bean
    public JwtUtil jwtUtil(@Value("${jwt.secret}") String secret) {
        return new JwtUtil(secret);
    }
}

