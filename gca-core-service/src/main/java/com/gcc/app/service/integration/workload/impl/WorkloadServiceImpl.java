package com.gcc.app.service.integration.workload.impl;

import com.gcc.app.service.integration.workload.WorkloadClient;
import com.gcc.app.service.integration.workload.WorkloadService;
import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkloadServiceImpl implements WorkloadService {

    private final WorkloadClient client;

    @Override
    public void processTrainerWorkload(TrainerWorkloadRequestDto request) {
        client.processWorkload(request);
    }

    @Override
    public TrainerSummaryResponseDto getTrainerSummary(String username) {
        return client.getTrainerSummary(username);
    }
}