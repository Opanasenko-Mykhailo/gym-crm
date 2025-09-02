package com.gcm.service;

import com.gcm.app.rest.TrainerSummaryRequest;
import com.gcm.app.rest.TrainerWorkloadRequest;

public interface WorkloadService {
    void processTrainerWorkload(TrainerWorkloadRequest request, String transactionId);

    TrainerSummaryRequest getTrainerSummary(String username);
}
