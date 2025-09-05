package com.gcm.service.impl;

import com.gcm.app.rest.TrainerSummaryResponse;
import com.gcm.app.rest.TrainerWorkloadRequest;
import com.gcm.mapper.TrainerSummaryMapper;
import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.YearlySummary;
import com.gcm.repository.TrainerSummaryRepository;
import com.gcm.util.TransactionLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static com.gcm.app.rest.TrainerWorkloadRequest.ActionTypeEnum.ADD;
import static com.gcm.app.rest.TrainerWorkloadRequest.ActionTypeEnum.DELETE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceImplTest {

    @Mock
    private TrainerSummaryRepository trainerRepo;

    @Mock
    private TrainerSummaryMapper trainerMapper;

    @Mock
    private TransactionLogger logger;

    @InjectMocks
    private WorkloadServiceImpl service;

    @Test
    void givenNewTrainer_whenProcessAddWorkload_thenTrainerCreated() {
        TrainerWorkloadRequest request = createWorkloadRequest("alice.smith", ADD, 45, LocalDate.of(2025, 9, 1));
        when(trainerRepo.findByUsername("alice.smith")).thenReturn(Optional.empty());

        service.processTrainerWorkload(request, "txn-001");

        ArgumentCaptor<TrainerSummary> captor = ArgumentCaptor.forClass(TrainerSummary.class);
        verify(trainerRepo).save(captor.capture());
        TrainerSummary saved = captor.getValue();

        assertThat(saved.getUsername()).isEqualTo("alice.smith");
        assertThat(saved.getFirstName()).isEqualTo("Alice");
        assertThat(saved.getLastName()).isEqualTo("Smith");
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getYears()).hasSize(1);
        assertThat(saved.getYears().get(0).getMonths().get(0).getTotalDurationMinutes()).isEqualTo(45);

        verify(logger).log("txn-001", "Processing workload for alice.smith");
        verify(logger).log("txn-001", "Updated trainer summary for alice.smith");
    }

    @Test
    void givenExistingTrainer_whenDeleteWorkload_thenDurationReduced() {
        TrainerSummary trainer = createTrainerWithWorkload();
        TrainerWorkloadRequest request = createWorkloadRequest("bob.jones", DELETE, 30, LocalDate.of(2025, 9, 2));

        when(trainerRepo.findByUsername("bob.jones")).thenReturn(Optional.of(trainer));

        service.processTrainerWorkload(request, "txn-002");

        MonthlySummary month = trainer.getYears().get(0).getMonths().get(0);
        assertThat(month.getTotalDurationMinutes()).isEqualTo(90);
        verify(trainerRepo).save(trainer);
        verify(logger).log("txn-002", "Processing workload for bob.jones");
        verify(logger).log("txn-002", "Updated trainer summary for bob.jones");
    }

    @Test
    void givenTrainer_whenGetTrainerSummary_thenMappedToRestModel() {
        TrainerSummary trainer = createTrainer("charlie.brown");
        TrainerSummaryResponse mapped = new TrainerSummaryResponse();

        mapped.setUsername("charlie.brown");

        when(trainerRepo.findByUsername("charlie.brown")).thenReturn(Optional.of(trainer));
        when(trainerMapper.toRestModel(trainer)).thenReturn(mapped);

        TrainerSummaryResponse result = service.getTrainerSummary("charlie.brown");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("charlie.brown");
    }

    private TrainerWorkloadRequest createWorkloadRequest(String username, TrainerWorkloadRequest.ActionTypeEnum actionType, int duration, LocalDate date) {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setUsername(username);
        request.setFirstName(capitalize(username.split("\\.")[0]));
        request.setLastName(capitalize(username.split("\\.")[1]));
        request.setActive(true);
        request.setTrainingDate(date);
        request.setDurationInMinutes(duration);
        request.setActionType(actionType);

        return request;
    }

    private TrainerSummary createTrainer(String username) {
        TrainerSummary trainer = new TrainerSummary();
        trainer.setUsername(username);
        trainer.setFirstName(capitalize(username.split("\\.")[0]));
        trainer.setLastName(capitalize(username.split("\\.")[1]));
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>());

        return trainer;
    }

    private TrainerSummary createTrainerWithWorkload() {
        TrainerSummary trainer = createTrainer("bob.jones");

        YearlySummary yearly = new YearlySummary();
        yearly.setYearNumber(2025);
        yearly.setTrainerSummary(trainer);
        yearly.setMonths(new ArrayList<>());

        MonthlySummary monthly = new MonthlySummary();
        monthly.setMonthNumber(9);
        monthly.setTotalDurationMinutes(120);
        monthly.setYearlySummary(yearly);

        yearly.getMonths().add(monthly);
        trainer.getYears().add(yearly);

        return trainer;
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}