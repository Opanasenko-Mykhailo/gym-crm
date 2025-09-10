package com.gcm.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionLoggingFilterTest {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_MDC = "transactionId";

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private TransactionLoggingFilter filter;

    @Test
    void givenRequestWithoutTransactionId_whenFilter_thenGeneratesTransactionIdAndLogs() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenReturn(Mono.empty());

        Mono<Void> actual = filter.filter(exchange, chain);

        StepVerifier.create(actual).verifyComplete();
        verify(chain, times(1)).filter(any());
        String transactionId = MDC.get(TRANSACTION_ID_MDC);
        assertThat(transactionId).isNotNull();
        assertThat(transactionId).isNotEmpty();
    }

    @Test
    void givenRequestWithTransactionId_whenFilter_thenUsesExistingTransactionId() {
        String existingId = "12345";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/test")
                .header(TRANSACTION_ID_HEADER, existingId)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(any())).thenReturn(Mono.empty());

        Mono<Void> actual = filter.filter(exchange, chain);

        StepVerifier.create(actual).verifyComplete();
        verify(chain, times(1)).filter(any());
        String transactionId = MDC.get(TRANSACTION_ID_MDC);
        assertThat(transactionId).isEqualTo(existingId);
    }
}
