package com.gcc.app.service.integration.workload;

import com.gcc.app.exception.MicroserviceUnavailableException;
import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
        log.info("Processing trainer workload for request: {}", request);

        webClientBuilder.build()
                .post()
                .uri(baseUrl + "/api/workload")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @CircuitBreaker(name = "workload-service", fallbackMethod = "getTrainerSummaryFallback")
    public TrainerSummaryResponseDto getTrainerSummary(String username) {
        log.info("Getting trainer summary for username: {}", username);

        return webClientBuilder.build()
                .get()
                .uri(baseUrl + "/api/workload/{username}", username)
                .retrieve()
                .bodyToMono(TrainerSummaryResponseDto.class)
                .block();
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