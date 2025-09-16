package com.gcc.app.integration.workload;

import com.gcc.app.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.integration.workload.dto.TrainerWorkloadRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadService {

    private final WorkloadMessageSender messageSender;
    private final WorkloadHttpClient httpClient;

    public void notifyWorkloadChange(TrainerWorkloadRequestDto request) {
        log.info("Notifying workload service about trainer workload change: {}", request);
        messageSender.sendTrainerWorkload(request);
    }

    public TrainerSummaryResponseDto getTrainerSummary(String username) {
        log.info("Retrieving trainer summary for: {}", username);

        return httpClient.getTrainerSummary(username);
    }
}