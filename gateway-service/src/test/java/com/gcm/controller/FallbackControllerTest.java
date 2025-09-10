package com.gcm.controller;

import com.gcm.security.JwtService;
import com.gcm.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.mockito.Mockito.when;

@Import(SecurityConfig.class)
@WebFluxTest(FallbackController.class)
@AutoConfigureWebTestClient
class FallbackControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldReturnFallbackMessage() {
        String token = "valid.jwt.token";
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("test.user");
        when(jwtService.getAuthorities(token)).thenReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")));

        webTestClient.get()
                .uri("/fallback/test-service")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class)
                .isEqualTo("Service [test-service] is temporarily unavailable. Please try again later.");
    }
}