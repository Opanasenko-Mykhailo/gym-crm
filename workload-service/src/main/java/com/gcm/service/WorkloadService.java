package com.gcm.service;

import com.gcm.service.dto.TrainerSummaryResponseDto;
import com.gcm.service.dto.TrainerWorkloadRequestDto;

public interface WorkloadService {
    void processTrainerWorkload(TrainerWorkloadRequestDto dto);

    TrainerSummaryResponseDto getTrainerSummary(String username);
}