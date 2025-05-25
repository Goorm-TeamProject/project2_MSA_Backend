package com.eouil.msa.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import java.util.List;

public class RequestHeaderToRequestUriGatewayFilterFactory extends AbstractGatewayFilterFactory
        <RequestHeaderToRequestUriGatewayFilterFactory.Config> {

    public RequestHeaderToRequestUriGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            HttpHeaders headers = request.getHeaders();
            List<String> cookieHeaders = headers.get(HttpHeaders.COOKIE);

            if (cookieHeaders != null && !cookieHeaders.isEmpty()) {
                String cookies = cookieHeaders.get(0);
                Pattern pattern = Pattern.compile("accessToken=([^1]*)");
                Matcher matcher = pattern.matcher(cookies);

                if (matcher.find()) {
                    String token = matcher.group(1);
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .build();
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                }
            }

            return chain.filter(exchange);
        };
    }

    @Getter @Setter
    public static class Config {
        private String name;
        private String match;
        private String value;
        private String headerName;


    }
}
