package com.gcc.app.service.integration.workload;

import com.gcc.app.exception.JmsMessageException;
import com.gcc.app.exception.MicroserviceUnavailableException;
import com.gcc.app.exception.UserNotAuthenticatedException;
import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceConnector {
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";

    private final WebClient.Builder webClientBuilder;
    private final JmsTemplate jmsTemplate;

    @Value("${workload.service.base-url}")
    private String baseUrl;

    @Value("${workload.queue.name}")
    private String workloadQueue;

    public void processTrainerWorkload(TrainerWorkloadRequestDto request) {
        String transactionId = getCurrentTransactionId();
        log.info("Sending trainer workload request via JMS. payload={}", request);

        try {
            jmsTemplate.convertAndSend(workloadQueue, request, message -> {
                message.setStringProperty(TRANSACTION_ID_HEADER, transactionId);
                return message;
            });
        } catch (Exception ex) {
            throw new JmsMessageException(
                    "Failed to send workload request to JMS queue", ex);
        }
    }

    @CircuitBreaker(name = "workload-service", fallbackMethod = "getTrainerSummaryFallback")
    public TrainerSummaryResponseDto getTrainerSummary(String username) {
        String token = getCurrentUserToken();
        String transactionId = getCurrentTransactionId();

        log.info("Getting trainer summary for username: {}", username);

        return webClientBuilder.build()
                .get()
                .uri(baseUrl + "/api/workload/{username}", username)
                .header("Authorization", "Bearer " + token)
                .header(TRANSACTION_ID_HEADER, transactionId)
                .retrieve()
                .bodyToMono(TrainerSummaryResponseDto.class)
                .block();
    }

    private String getCurrentUserToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getCredentials() instanceof String) {
            return (String) auth.getCredentials();
        }
        throw new UserNotAuthenticatedException("No JWT token found for current user");
    }

    private String getCurrentTransactionId() {
        String transactionId = MDC.get("transactionId");
        return transactionId != null ? transactionId : UUID.randomUUID().toString();
    }

    public TrainerSummaryResponseDto getTrainerSummaryFallback(String username, Exception ex) {
        throw new MicroserviceUnavailableException(
                "Workload service is temporarily unavailable for retrieving trainer summary", ex);
    }
}