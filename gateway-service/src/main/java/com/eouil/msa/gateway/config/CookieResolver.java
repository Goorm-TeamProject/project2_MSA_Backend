package com.eouil.msa.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CookieResolver {

    public String resolveToken(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst("accessToken");
        String token = cookie != null ? cookie.getValue() : "";
        log.debug("Resolved token from cookie: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        return token;
    }
}