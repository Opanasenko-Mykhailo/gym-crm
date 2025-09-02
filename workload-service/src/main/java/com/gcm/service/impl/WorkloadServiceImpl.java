package com.gcm.service.impl;

import com.gcm.app.rest.TrainerSummaryRequest;
import com.gcm.app.rest.TrainerWorkloadRequest;
import com.gcm.mapper.TrainerSummaryMapper;
import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.YearlySummary;
import com.gcm.repository.TrainerSummaryRepository;
import com.gcm.service.WorkloadService;
import com.gcm.util.TransactionLogger;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class WorkloadServiceImpl implements WorkloadService {

    private final TrainerSummaryRepository trainerRepo;
    private final TrainerSummaryMapper trainerMapper;
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
    public TrainerSummaryRequest getTrainerSummary(String username) {
        return trainerMapper.toRestModel(
                trainerRepo.findByUsername(username).orElse(null)
        );
    }

    private TrainerSummary getOrCreateTrainer(TrainerWorkloadRequest request) {
        return trainerRepo.findByUsername(request.getUsername())
                .orElseGet(() -> TrainerSummary.builder()
                        .username(request.getUsername())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .active(request.getActive())
                        .years(new ArrayList<>())
                        .build());
    }

    private YearlySummary getOrCreateYearlySummary(TrainerSummary trainer, int year) {
        if (trainer.getYears() == null) {
            trainer.setYears(new ArrayList<>());
        }

        return trainer.getYears().stream()
                .filter(y -> y.getYearNumber() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearlySummary y = YearlySummary.builder()
                            .yearNumber(year)
                            .trainerSummary(trainer)
                            .months(new ArrayList<>())
                            .build();
                    trainer.getYears().add(y);
                    return y;
                });
    }

    private MonthlySummary getOrCreateMonthlySummary(YearlySummary yearly, int month) {
        if (yearly.getMonths() == null) {
            yearly.setMonths(new ArrayList<>());
        }

        return yearly.getMonths().stream()
                .filter(m -> m.getMonthNumber() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthlySummary m = MonthlySummary.builder()
                            .monthNumber(month)
                            .totalDurationMinutes(0)
                            .yearlySummary(yearly)
                            .build();
                    yearly.getMonths().add(m);
                    return m;
                });
    }

    private void updateMonthlyDuration(MonthlySummary monthly, TrainerWorkloadRequest request) {
        switch (request.getActionType()) {
            case ADD ->
                    monthly.setTotalDurationMinutes(monthly.getTotalDurationMinutes() + request.getDurationInMinutes());
            case DELETE ->
                    monthly.setTotalDurationMinutes(Math.max(0, monthly.getTotalDurationMinutes() - request.getDurationInMinutes()));
        }
    }
}