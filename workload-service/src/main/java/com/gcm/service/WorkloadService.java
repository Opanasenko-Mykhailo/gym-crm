package com.gcm.service;

import com.gcm.service.dto.TrainerSummaryResponseDto;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
import jakarta.validation.Valid;

public interface WorkloadService {
    void processTrainerWorkload(@Valid TrainerWorkloadRequestDto dto);

    TrainerSummaryResponseDto getTrainerSummary(String username);
}