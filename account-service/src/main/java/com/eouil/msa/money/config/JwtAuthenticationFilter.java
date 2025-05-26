// src/main/java/com/eouil/msa/money/config/JwtAuthenticationFilter.java
package com.eouil.msa.money.config;

import com.eouil.msa.shared.redis.RedisTokenService;
import com.eouil.msa.shared.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        // 퍼밋 경로들
        if (path.startsWith("/api/join") ||
                path.startsWith("/api/login") ||
                path.startsWith("/api/refresh") ||
                path.startsWith("/api/logout") ||
                path.startsWith("/api/mfa")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 1) 헤더 또는 쿠키에서 토큰 추출
        String token = resolveToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId;
        // 2) 서명 검증 및 userId 추출
        try {
            userId = jwtUtil.validateTokenAndGetUserId(token);
        } catch (Exception ex) {
            log.warn("Invalid JWT token", ex);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

        // 3) 블랙리스트 체크
        if (redisTokenService.isBlacklisted(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token blacklisted");
            return;
        }

        // 4) Authentication 생성 & SecurityContext 세팅
        var auth = jwtUtil.validateTokenAndGetAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("accessToken".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
