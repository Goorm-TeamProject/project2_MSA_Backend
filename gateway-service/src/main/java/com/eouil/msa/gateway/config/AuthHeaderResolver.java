package com.eouil.msa.gateway.config;

import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthHeaderResolver {

    public String resolve(ServerHttpRequest request) {
        HttpCookie cookie = request.getCookies().getFirst("accessToken");
        return cookie != null ? cookie.getValue() : "";
    }
}
