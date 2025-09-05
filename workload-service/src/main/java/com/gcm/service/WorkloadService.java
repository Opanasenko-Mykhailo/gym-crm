package com.gcm.service;

import com.gcm.app.rest.TrainerSummaryResponse;
import com.gcm.app.rest.TrainerWorkloadRequest;

public interface WorkloadService {
    void processTrainerWorkload(TrainerWorkloadRequest request, String transactionId);

    TrainerSummaryResponse getTrainerSummary(String username);
}