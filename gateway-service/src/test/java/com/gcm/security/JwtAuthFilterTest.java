package com.gcm.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final String TEST_TOKEN = "dummy-token";
    private static final String TEST_USERNAME = "alice.smith";

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void givenValidToken_whenFilter_thenAuthenticationSetInContext() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtService.isTokenValid(TEST_TOKEN)).thenReturn(true);
        when(jwtService.extractUsername(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(jwtService.getAuthorities(TEST_TOKEN)).thenReturn(List.of());

        Mono<Void> actual = jwtAuthFilter.filter(exchange, (ex) -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .doOnNext(auth -> {
                    assertThat(auth).isNotNull();
                    assertThat(auth.getName()).isEqualTo(TEST_USERNAME);})
                .then());

        StepVerifier.create(actual)
                .verifyComplete();

        verify(jwtService).isTokenValid(TEST_TOKEN);
        verify(jwtService).extractUsername(TEST_TOKEN);
        verify(jwtService).getAuthorities(TEST_TOKEN);
    }

    @Test
    void givenNoToken_whenFilter_thenNoAuthenticationSet() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        Mono<Void> actual = jwtAuthFilter.filter(exchange, (ex) -> Mono.empty());

        StepVerifier.create(actual)
                .verifyComplete();

        ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> Mono.just(ctx.getAuthentication() != null))
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        verifyNoInteractions(jwtService);
    }

    @Test
    void givenInvalidToken_whenFilter_thenNoAuthenticationSet() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_TOKEN)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(jwtService.isTokenValid(TEST_TOKEN)).thenReturn(false);

        Mono<Void> actual = jwtAuthFilter.filter(exchange, (ex) -> Mono.empty());

        StepVerifier.create(actual)
                .verifyComplete();

        ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> Mono.just(ctx.getAuthentication() != null))
                .as(StepVerifier::create)
                .expectComplete()
                .verify();

        verify(jwtService).isTokenValid(TEST_TOKEN);
        verify(jwtService, never()).extractUsername(any());
        verify(jwtService, never()).getAuthorities(any());
    }
}