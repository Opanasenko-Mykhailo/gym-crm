package com.gcm.service;

import com.gcm.model.TrainerSummary;
import com.gcm.model.TrainerWorkloadRequest;

public interface WorkloadService {
    public void processTrainerWorkload(TrainerWorkloadRequest request, String transactionId);
    public TrainerSummary getTrainerSummary(String username);
}
