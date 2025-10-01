package com.gcm.steps;

import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.YearlySummary;
import com.gcm.repository.TrainerSummaryRepository;
import com.gcm.service.WorkloadService;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
import com.gcm.service.dto.TrainerWorkloadRequestDto.ActionType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
        Optional<TrainerSummary> summaryOpt = trainerRepo.findByUsername(username);
        assertThat(summaryOpt).isPresent();

        TrainerSummary summary = summaryOpt.get();
        List<YearlySummary> years = summary.getYears();
        assertThat(years).isNotEmpty();

        YearlySummary targetYearly = years.get(0);
        assertThat(targetYearly.getYearNumber()).isEqualTo(year);

        List<MonthlySummary> monthsList = targetYearly.getMonths();
        assertThat(monthsList).isNotEmpty();

        MonthlySummary targetMonthly = monthsList.get(0);

        if (targetMonthly.getMonthNumber() != month) {
            targetMonthly = monthsList.get(1);
        }

        assertThat(targetMonthly.getMonthNumber()).isEqualTo(month);
        assertThat(targetMonthly.getTotalDurationMinutes()).isEqualTo(minutes);
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