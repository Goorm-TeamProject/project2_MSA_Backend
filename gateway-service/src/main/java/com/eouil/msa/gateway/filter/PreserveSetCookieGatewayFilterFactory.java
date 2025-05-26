package com.eouil.msa.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class PreserveSetCookieGatewayFilterFactory
        extends AbstractGatewayFilterFactory<PreserveSetCookieGatewayFilterFactory.Config> {

    public static class Config {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public PreserveSetCookieGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            return chain.filter(exchange).then().contextWrite(ctx -> {
                if (config.isEnabled()) {
                    ServerHttpResponse response = exchange.getResponse();
                    // Set-Cookie 헤더를 보존
                    response.getHeaders().entrySet().stream()
                            .filter(entry -> entry.getKey().equalsIgnoreCase("Set-Cookie"))
                            .forEach(entry -> response.getHeaders().addAll(entry.getKey(), entry.getValue()));
                }
                return ctx;
            });
        };
    }
}