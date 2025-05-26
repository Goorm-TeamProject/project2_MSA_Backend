// src/main/java/com/eouil/msa/gateway/filter/CookieToHeaderFilter.java
package com.eouil.msa.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
@Slf4j
public class CookieToHeaderGatewayFilterFactory
        extends AbstractGatewayFilterFactory<CookieToHeaderGatewayFilterFactory.Config> {

    /** 필터 옵션이 없으므로 빈 껍데기 */
    public static class Config { }

    public CookieToHeaderGatewayFilterFactory() {
        super(Config.class);
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            String method = exchange.getRequest().getMethod() != null
                    ? exchange.getRequest().getMethod().name()
                    : "";

            log.info("[CookieToHeader] Filter 시작 - Path: {}, Method: {}", path, method);

            if ("OPTIONS".equalsIgnoreCase(method)) {
                log.debug("[CookieToHeader] Preflight 요청, 필터 통과");
                return chain.filter(exchange);
            }

            if (path.startsWith("/api/users/join") ||
                    path.startsWith("/api/users/login") ||
                    path.startsWith("/api/users/logout") ||
                    path.startsWith("/api/users/health")) {
                log.debug("[CookieToHeader] 공개 엔드포인트 요청, 필터 통과: {}", path);
                return chain.filter(exchange);
            }

            String accessToken = getAccessTokenFromCookie(exchange);
            log.info("[CookieToHeader] 추출된 accessToken: {}", accessToken);

            if (accessToken != null) {
                HttpHeaders originalHeaders = exchange.getRequest().getHeaders();
                log.info("[CookieToHeader] 원본 헤더 클래스 타입: {}", originalHeaders.getClass());
                log.info("[CookieToHeader] 원본 헤더: {}", originalHeaders);

                HttpHeaders newHeaders = new HttpHeaders();
                newHeaders.putAll(originalHeaders);
                newHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
                log.info("[CookieToHeader] 새로 만든 헤더: {}", newHeaders);

                ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                    @Override
                    public HttpHeaders getHeaders() {
                        return newHeaders;
                    }
                };

                log.info("[CookieToHeader] JWT 추가된 요청으로 교체 후 필터 통과");

                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            }

            log.info("[CookieToHeader] 토큰 없음, 원본 요청 필터 통과");
            return chain.filter(exchange);
        };
    }



    private String getAccessTokenFromCookie(ServerWebExchange exchange) {
        return exchange.getRequest().getCookies().getFirst("accessToken") != null
                ? exchange.getRequest().getCookies().getFirst("accessToken").getValue()
                : null;
    }
}
