package com.gcc.app.service.integration.workload;

import com.gcc.app.service.integration.workload.dto.TrainerSummaryResponseDto;
import com.gcc.app.service.integration.workload.dto.TrainerWorkloadRequestDto;

public interface WorkloadService {
    void processTrainerWorkload(TrainerWorkloadRequestDto request);

    TrainerSummaryResponseDto getTrainerSummary(String username);
}