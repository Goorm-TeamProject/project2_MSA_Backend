package com.eouil.msa.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                // CSRF 비활성화
                .csrf(csrf -> csrf.disable())
                // HTTP Basic 비활성화 (401 Unauthorized 방지)
                .httpBasic(basic -> basic.disable())
                // Form 로그인 비활성화
                .formLogin(form -> form.disable())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, exAuth) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return Mono.empty();
                        })
                )

                // 엔드포인트별 접근 제어
                .authorizeExchange(exchanges -> exchanges
                        // Preflight 요청 허용
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 헬스체크 및 actuator 공개
                        .pathMatchers(
                                HttpMethod.GET,
                                "/actuator/health/**",
                                "/actuator/info/**"
                        ).permitAll()

                        // 회원가입/로그인 API 공개
                        .pathMatchers(HttpMethod.POST,
                                "/api/users/join",
                                "/api/users/login",
                                "/api/users/logout",
                                "/api/users/refresh",
                                "/api/refresh"
                        ).permitAll()

                        // 그 외 /api/** 요청은 인증 필요
                        .pathMatchers("/api/**").permitAll()

                        // 그 외 모든 요청은 허용
                        .anyExchange().permitAll()
                )

                // 상태 비저장 (세션 사용 안 함)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http.build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(
                "https://eouil.com",
                "https://api.eouil.com",
                "http://localhost:5173"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Set-Cookie","Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}
