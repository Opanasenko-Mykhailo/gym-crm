package com.gcm.service.impl;

import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.TrainerWorkloadRequest;
import com.gcm.model.YearlySummary;
import com.gcm.repository.TrainerSummaryRepository;
import com.gcm.service.WorkloadService;
import com.gcm.util.TransactionLogger;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkloadServiceImpl implements WorkloadService {

    private final TrainerSummaryRepository trainerRepo;
    private final TransactionLogger logger;

    @Override
    @Transactional
    public void processTrainerWorkload(TrainerWorkloadRequest request, String transactionId) {
        logger.log(transactionId, "Processing workload for " + request.getUsername());

        TrainerSummary trainer = getOrCreateTrainer(request);
        YearlySummary yearly = getOrCreateYearlySummary(trainer, request.getTrainingDate().getYear());
        MonthlySummary monthly = getOrCreateMonthlySummary(yearly, request.getTrainingDate().getMonthValue());

        updateMonthlyDuration(monthly, request);

        trainerRepo.save(trainer);
        logger.log(transactionId, "Updated trainer summary for " + request.getUsername());
    }

    @Override
    public TrainerSummary getTrainerSummary(String username) {
        return trainerRepo.findByUsername(username).orElse(null);
    }

    private TrainerSummary getOrCreateTrainer(TrainerWorkloadRequest request) {
        return trainerRepo.findByUsername(request.getUsername())
                .orElseGet(() -> {
                    TrainerSummary t = new TrainerSummary();
                    t.setUsername(request.getUsername());
                    t.setFirstName(request.getFirstName());
                    t.setLastName(request.getLastName());
                    t.setActive(request.isActive());
                    return t;
                });
    }

    private YearlySummary getOrCreateYearlySummary(TrainerSummary trainer, int year) {
        return trainer.getYears().stream()
                .filter(y -> y.getYearNumber() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearlySummary y = new YearlySummary();
                    y.setYearNumber(year);
                    y.setTrainerSummary(trainer);
                    trainer.getYears().add(y);
                    return y;
                });
    }

    private MonthlySummary getOrCreateMonthlySummary(YearlySummary yearly, int month) {
        return yearly.getMonths().stream()
                .filter(m -> m.getMonthNumber() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthlySummary m = new MonthlySummary();
                    m.setMonthNumber(month);
                    m.setTotalDurationMinutes(0);
                    m.setYearlySummary(yearly);
                    yearly.getMonths().add(m);
                    return m;
                });
    }

    private void updateMonthlyDuration(MonthlySummary monthly, TrainerWorkloadRequest request) {
        if ("ADD".equalsIgnoreCase(request.getActionType())) {
            monthly.setTotalDurationMinutes(monthly.getTotalDurationMinutes() + request.getDurationInMinutes());
        } else if ("DELETE".equalsIgnoreCase(request.getActionType())) {
            monthly.setTotalDurationMinutes(Math.max(0, monthly.getTotalDurationMinutes() - request.getDurationInMinutes()));
        }
    }
}