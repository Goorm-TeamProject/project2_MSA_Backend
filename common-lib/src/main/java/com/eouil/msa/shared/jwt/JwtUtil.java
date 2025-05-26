package com.eouil.msa.shared.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    private final Key key;
    private final long ACCESS_EXP = 1000 * 60 * 5;  // 5분
    private final long REFRESH_EXP = 1000 * 60 * 60 * 24 * 7; // 7일


    public JwtUtil(@Value("${jwt.secret}") String secretKey, Environment env) {

        byte[] decodedKey = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(decodedKey);
        // 환경변수 확인 로그

        System.out.println("[JWT] Loaded JWT_SECRET: " + env.getProperty("jwt.secret"));
        // 또는 로그 사용
        log.info("[JWT] Loaded JWT_SECRET: {}", env.getProperty("jwt.secret"));
    }

    // access token
    public String generateAccessToken(String userId) {
        try {
            return Jwts.builder()
                    .setSubject(userId)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXP))
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("accessToken 생성 실패", e);
        }
    }

    public Authentication validateTokenAndGetAuthentication(String token) {
        // 1) 토큰에서 userId 추출
        String userId = validateTokenAndGetUserId(token);

        // 2) 추출된 userId로 Authentication 객체 생성
        //    권한이 없다면 빈 리스트, 필요시 Roles/Authorities를 claim에서 꺼내 세팅
        return new UsernamePasswordAuthenticationToken(
                userId,          // principal
                null,            // credentials
                Collections.emptyList() // authorities
        );
    }

    // refresh token
    public String generateRefreshToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXP))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // JWT 토큰을 검증하고 userId 추출
    public String validateTokenAndGetUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60) // ← 1분 정도 시간 차 허용
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();

        return claims.getSubject();
    }

    public long getAccessTokenExpireMillis() {
        return ACCESS_EXP;
    }

    public long getRefreshTokenExpireMillis() {
        return REFRESH_EXP;
    }

    public long getExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();

        return claims.getExpiration().getTime();
    }
    
}