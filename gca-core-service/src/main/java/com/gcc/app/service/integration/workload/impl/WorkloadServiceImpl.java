package com.gcc.app.service.integration.workload.impl;

import com.gcc.app.service.integration.workload.WorkloadService;
import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class WorkloadServiceImpl implements WorkloadService {

    private final WebClient.Builder webClientBuilder;

    @Value("${workload.service.base-url}")
    private String baseUrl;

    @Override
    public void processTrainerWorkload(TrainerWorkloadRequestDto request) {
        webClientBuilder.build()
                .post()
                .uri(baseUrl + "/api/workload")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Override
    public TrainerSummaryResponseDto getTrainerSummary(String username) {
        return webClientBuilder.build()
                .get()
                .uri(baseUrl + "/api/workload/{username}", username)
                .retrieve()
                .bodyToMono(TrainerSummaryResponseDto.class)
                .block();
    }
}