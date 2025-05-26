// src/main/java/com/eouil/msa/users/filter/AuthenticationFilter.java
package com.eouil.msa.users.filter;

import com.eouil.msa.users.domains.CustomUserDetails;
import com.eouil.msa.users.domains.User;
import com.eouil.msa.users.dtos.LoginRequest;
import com.eouil.msa.users.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final UserRepository userRepository;
    private final Environment env;

    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UserRepository userRepository,
                                Environment env) {
        super.setAuthenticationManager(authenticationManager);
        this.userRepository = userRepository;
        this.env = env;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {
        try {
            LoginRequest creds = new ObjectMapper()
                    .readValue(request.getInputStream(), LoginRequest.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getEmail(),
                            creds.getPassword(),
                            new ArrayList<>()
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {

        log.debug("[successfulAuthentication] Principal 타입: {}", authResult.getPrincipal().getClass().getName());
        log.debug("[successfulAuthentication] Principal 정보: {}", authResult.getPrincipal());
        log.debug("[successfulAuthentication] 인증된 이름(getName): {}", authResult.getName());

        String userId = authResult.getName();  // getName() == getUsername() == userId

        long expirationMillis = Long.parseLong(env.getProperty("jwt.expiration_time"));
        log.debug("[successfulAuthentication] JWT 만료시간(ms): {}", expirationMillis);

        String jwtSecret = env.getProperty("jwt.secret");
        log.debug("[successfulAuthentication] JWT 시크릿: {}", jwtSecret);

        String accessToken = Jwts.builder()
                .setSubject(userId)
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        log.debug("[successfulAuthentication] 생성된 AccessToken: {}", accessToken);

        int maxAge = (int) (expirationMillis / 1000);
        String cookie = String.format(
                "accessToken=%s; HttpOnly; Secure; SameSite=None; Path=/; Max-Age=%d",
                accessToken, maxAge
        );
        response.addHeader(HttpHeaders.SET_COOKIE, cookie);

        log.info("로그인 성공, accessToken 쿠키 발급: userId={}, maxAge(s)={}", userId, maxAge);
    }



}
