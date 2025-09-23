package com.gcm.service.impl;

import com.gcm.mapper.TrainerSummaryMapper;
import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.YearlySummary;
import com.gcm.repository.TrainerSummaryRepository;
import com.gcm.service.WorkloadService;
import com.gcm.service.dto.TrainerSummaryResponseDto;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkloadServiceImpl implements WorkloadService {

    private final TrainerSummaryRepository trainerRepo;
    private final TrainerSummaryMapper trainerMapper;

    @Override
    @Transactional
    public void processTrainerWorkload(TrainerWorkloadRequestDto request) {
        log.info("Processing workload for {}", request.getUsername());

        TrainerSummary trainer = getOrCreateTrainer(request);
        YearlySummary yearly = getOrCreateYearlySummary(trainer, request.getTrainingDate().getYear());
        MonthlySummary monthly = getOrCreateMonthlySummary(yearly, request.getTrainingDate().getMonthValue());

        updateMonthlyDuration(monthly, request);

        trainerRepo.save(trainer);
        log.info("Updated trainer summary for {}", request.getUsername());
    }

    @Override
    public TrainerSummaryResponseDto getTrainerSummary(String username) {
        return trainerMapper.toDto(
                trainerRepo.findByUsername(username).orElse(null)
        );
    }

    private TrainerSummary getOrCreateTrainer(TrainerWorkloadRequestDto request) {
        return trainerRepo.findByUsername(request.getUsername())
                .orElseGet(() -> buildNewTrainerSummary(request));
    }

    private YearlySummary getOrCreateYearlySummary(TrainerSummary trainer, int year) {
        if (trainer.getYears() == null) {
            trainer.setYears(new ArrayList<>());
        }

        return trainer.getYears().stream()
                .filter(y -> y.getYearNumber() == year)
                .findFirst()
                .orElseGet(() -> buildNewYearlySummary(trainer, year));
    }

    private MonthlySummary getOrCreateMonthlySummary(YearlySummary yearly, int month) {
        if (yearly.getMonths() == null) {
            yearly.setMonths(new ArrayList<>());
        }

        return yearly.getMonths().stream()
                .filter(m -> m.getMonthNumber() == month)
                .findFirst()
                .orElseGet(() -> buildNewMonthlySummary(yearly, month));
    }

    private TrainerSummary buildNewTrainerSummary(TrainerWorkloadRequestDto request) {
        return TrainerSummary.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .active(request.isActive())
                .years(new ArrayList<>())
                .build();
    }

    private YearlySummary buildNewYearlySummary(TrainerSummary trainer, int year) {
        YearlySummary yearly = YearlySummary.builder()
                .yearNumber(year)
                .months(new ArrayList<>())
                .build();

        trainer.getYears().add(yearly);

        return yearly;
    }

    private MonthlySummary buildNewMonthlySummary(YearlySummary yearly, int month) {
        MonthlySummary monthly = MonthlySummary.builder()
                .monthNumber(month)
                .totalDurationMinutes(0)
                .build();

        yearly.getMonths().add(monthly);

        return monthly;
    }

    private void updateMonthlyDuration(MonthlySummary monthly, TrainerWorkloadRequestDto request) {
        switch (request.getActionType()) {
            case ADD ->
                    monthly.setTotalDurationMinutes(monthly.getTotalDurationMinutes() + request.getDurationInMinutes().intValue());
            case DELETE ->
                    monthly.setTotalDurationMinutes(Math.max(0, monthly.getTotalDurationMinutes() - request.getDurationInMinutes().intValue()));
        }
    }
}