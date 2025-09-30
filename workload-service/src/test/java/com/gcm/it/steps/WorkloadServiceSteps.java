package com.gcm.it.steps;

import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.repository.TrainerSummaryRepository;
import com.gcm.service.WorkloadService;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
import com.gcm.service.dto.TrainerWorkloadRequestDto.ActionType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolationException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class WorkloadServiceSteps {

    private static final String DEFAULT_FIRST_NAME = "Jane";
    private static final String DEFAULT_LAST_NAME = "Silver";
    private static final boolean DEFAULT_ACTIVE = true;

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private TrainerSummaryRepository trainerRepo;

    @Given("a trainer exists with username {string}")
    public void a_trainer_exists(String username) {
        trainerRepo.findByUsername(username)
                .orElseGet(() -> trainerRepo.save(createTrainer(username)));
    }

    @When("I add a workload of {int} minutes for {string} in month {int} of {int}")
    public void i_add_workload(int minutes, String username, int month, int year) {
        processWorkload(username, minutes, month, year, ActionType.ADD);
    }

    @When("I remove a workload of {int} minutes for {string} in month {int} of {int}")
    public void i_remove_workload(int minutes, String username, int month, int year) {
        processWorkload(username, minutes, month, year, ActionType.DELETE);
    }

    @When("I try to add workload of {int} minutes for {string} in month {int} of {int}")
    public void i_try_to_add_invalid_workload(int minutes, String username, int month, int year) {
        assertThrows(ConstraintViolationException.class, () ->
                processWorkload(username, minutes, month, year, ActionType.ADD)
        );
    }

    @Then("the trainer summary for {string} should contain {int} minutes for month {int} of {int}")
    public void the_trainer_summary_should_contain(String username, int minutes, int month, int year) {
        TrainerSummary summary = trainerRepo.findByUsername(username).orElseThrow();
        MonthlySummary monthly = summary.getYears().stream()
                .filter(y -> y.getYearNumber() == year)
                .flatMap(y -> y.getMonths().stream())
                .filter(m -> m.getMonthNumber() == month)
                .findFirst()
                .orElseThrow();

        assertThat(monthly.getTotalDurationMinutes()).isEqualTo(minutes);
    }

    private TrainerSummary createTrainer(String username) {
        return TrainerSummary.builder()
                .username(username)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .active(DEFAULT_ACTIVE)
                .build();
    }

    private void processWorkload(String username, int minutes, int month, int year, ActionType actionType) {
        TrainerWorkloadRequestDto request = TrainerWorkloadRequestDto.builder()
                .username(username)
                .firstName(DEFAULT_FIRST_NAME)
                .lastName(DEFAULT_LAST_NAME)
                .active(DEFAULT_ACTIVE)
                .durationInMinutes((long) minutes)
                .actionType(actionType)
                .trainingDate(LocalDate.of(year, month, 1))
                .build();

        workloadService.processTrainerWorkload(request);
    }
}