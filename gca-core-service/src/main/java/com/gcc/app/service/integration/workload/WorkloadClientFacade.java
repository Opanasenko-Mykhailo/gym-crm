package com.gcc.app.service.integration.workload;

import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadClientFacade {

    private final WorkloadMessagingClient messagingClient;
    private final WorkloadSummaryClient summaryClient;

    public void notifyWorkloadService(TrainerWorkloadRequestDto request) {
        messagingClient.sendTrainerWorkload(request);
    }

    public TrainerSummaryResponseDto getTrainerSummary(String username) {
        return summaryClient.getTrainerSummary(username);
    }
}