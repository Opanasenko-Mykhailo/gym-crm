package com.gcm.controller;

import com.gcm.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@Import(SecurityConfig.class)
@WebFluxTest(FallbackController.class)
@AutoConfigureWebTestClient
class FallbackControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldReturnFallbackMessage() {
        webTestClient.get()
                .uri("/fallback/test-service")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                .expectBody(String.class)
                .isEqualTo("Service [test-service] is temporarily unavailable. Please try again later.");
    }
}