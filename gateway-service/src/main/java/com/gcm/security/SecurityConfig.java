package com.gcm.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(getPublicPaths()).permitAll()
                        .anyExchange().authenticated())
                .addFilterAt(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private String[] getPublicPaths() {
        return new String[] {
                "/actuator/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/public/**",
                "/gym-crm-core/api/v1/login",
                "/gym-crm-core/api/v1/refresh-token",
                "/gym-crm-core/api/v1/trainees/register",
                "/gym-crm-core/api/v1/trainers/register",
                "/gym-crm-core/api/v1/logout"
        };
    }
}