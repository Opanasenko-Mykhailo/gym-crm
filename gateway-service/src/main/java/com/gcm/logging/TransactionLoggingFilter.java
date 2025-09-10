package com.gcm.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class TransactionLoggingFilter implements GlobalFilter, Ordered {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_MDC = "transactionId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpHeaders headers = exchange.getRequest().getHeaders();

        String transactionId = Optional.ofNullable(headers.getFirst(TRANSACTION_ID_HEADER))
                .filter(StringUtils::hasText)
                .orElseGet(() -> UUID.randomUUID().toString());

        MDC.put(TRANSACTION_ID_MDC, transactionId);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(builder -> builder.headers(httpHeaders -> httpHeaders.set(TRANSACTION_ID_HEADER, transactionId)))
                .build();

        log.info("Incoming request: {} {}", mutatedExchange.getRequest().getMethod(), mutatedExchange.getRequest().getURI());

        return chain.filter(mutatedExchange).doFinally(signalType -> {mutatedExchange.getResponse().getStatusCode();
            int statusCode = mutatedExchange.getResponse().getStatusCode().value();
            log.info("Outgoing response: {} {} -> {}", mutatedExchange.getRequest().getMethod(), mutatedExchange.getRequest().getURI(), statusCode);

            MDC.remove(TRANSACTION_ID_MDC);
        });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}