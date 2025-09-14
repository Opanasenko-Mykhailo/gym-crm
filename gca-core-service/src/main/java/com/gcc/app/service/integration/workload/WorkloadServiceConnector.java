package com.gcc.app.service.integration.workload;

import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkloadServiceConnector {

    private final TrainerWorkloadSender workloadSender;
    private final TrainerSummaryClient summaryClient;

    public void processTrainerWorkload(TrainerWorkloadRequestDto request) {
        workloadSender.sendTrainerWorkload(request);
    }

    public TrainerSummaryResponseDto getTrainerSummary(String username) {
        return summaryClient.getTrainerSummary(username);
    }
}