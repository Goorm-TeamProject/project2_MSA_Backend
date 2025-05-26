package com.eouil.msa.gateway.filter;

import com.eouil.msa.shared.redis.RedisTokenService;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import io.jsonwebtoken.security.Keys;

import java.security.Key;

@Slf4j
@Component("AuthorizationHeader")
public class AuthorizationHeaderGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthorizationHeaderGatewayFilterFactory.Config> {


    public static class Config {
        // 나중에 필터 옵션을 추가하고 싶으면 여기에 필드 선언
    }

    private final Key key;
    private final Environment env;
    private final RedisTokenService redisTokenService;

    public AuthorizationHeaderGatewayFilterFactory(Environment env, RedisTokenService redisTokenService) {
        super(Config.class);
        this.env = env;
        this.redisTokenService = redisTokenService;

        String secret = env.getProperty("jwt.secret");
        if (secret == null) {
            throw new IllegalStateException("jwt.secret is not configured");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String method = exchange.getRequest().getMethod() != null
                    ? exchange.getRequest().getMethod().name()
                    : "";

            if ("OPTIONS".equalsIgnoreCase(method)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            log.debug("[AuthorizationHeader] Authorization 헤더: {}", authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("[AuthorizationHeader] 헤더 없음, 401 반환");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7).trim();
            log.debug("[AuthorizationHeader] 추출된 토큰: {}", token);

            try {
                // ⬇⬇⬇ 문제의 코드 (서명 검증 실패 발생하는 부분)
                Jwts.parser()
                        .setSigningKey(env.getProperty("jwt.secret"))
                        .parseClaimsJws(token);

                log.debug("[AuthorizationHeader] JWT 서명 검증 성공");

            } catch (Exception e) {
                log.warn("[AuthorizationHeader] JWT 검증 실패: {}", e.getMessage());
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            if (redisTokenService.isBlacklisted(token)) {
                log.warn("[AuthorizationHeader] 블랙리스트 토큰, 401 반환");
                return onError(exchange, HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        };
    }




    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
