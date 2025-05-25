package com.eouil.msa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.Arrays;

@Configuration
public class GatewayCorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(Arrays.asList("Set-Cookie"));

        org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource();  // reactive 버전 사용
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}