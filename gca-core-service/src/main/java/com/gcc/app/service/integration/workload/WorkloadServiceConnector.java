package com.gcc.app.service.integration.workload;

import com.gcc.app.exception.MicroserviceUnavailableException;
import com.gcc.app.exception.UserNotAuthenticatedException;
import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceConnector {

    private final WebClient.Builder webClientBuilder;

    @Value("${workload.service.base-url}")
    private String baseUrl;

    @CircuitBreaker(name = "workload-service", fallbackMethod = "processTrainerWorkloadFallback")
    public void processTrainerWorkload(TrainerWorkloadRequestDto request) {
        String token = getCurrentUserToken();

        log.info("Processing trainer workload for request: {}", request);

        webClientBuilder.build()
                .post()
                .uri(baseUrl + "/api/workload")
                .header("Authorization", "Bearer " + token)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @CircuitBreaker(name = "workload-service", fallbackMethod = "getTrainerSummaryFallback")
    public TrainerSummaryResponseDto getTrainerSummary(String username) {
        String token = getCurrentUserToken();

        log.info("Getting trainer summary for username: {}", username);

        return webClientBuilder.build()
                .get()
                .uri(baseUrl + "/api/workload/{username}", username)
                .header("Authorization", "Bearer " + token)
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

    public void processTrainerWorkloadFallback(TrainerWorkloadRequestDto request, Exception ex) {
        throw new MicroserviceUnavailableException(
                "Workload service is temporarily unavailable for processing trainer workload", ex);
    }

    public TrainerSummaryResponseDto getTrainerSummaryFallback(String username, Exception ex) {
        throw new MicroserviceUnavailableException(
                "Workload service is temporarily unavailable for retrieving trainer summary", ex);
    }
}