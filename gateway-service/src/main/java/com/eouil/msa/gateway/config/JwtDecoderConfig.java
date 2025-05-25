package com.eouil.msa.gateway.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtDecoderConfig {

    // application.yml 에 설정한 jwt.secret 값을 가져온다고 가정
    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        // 비밀키 문자열을 바이트 배열로 변환
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // io.jsonwebtoken.security.Keys 로 HMAC용 SecretKey 생성
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        // NimbusReactiveJwtDecoder 에 SecretKey 등록
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }
}
