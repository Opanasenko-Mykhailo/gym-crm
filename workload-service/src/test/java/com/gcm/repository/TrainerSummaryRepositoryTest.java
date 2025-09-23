package com.gcm.repository;

import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.YearlySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class TrainerSummaryRepositoryTest {

    @Autowired
    private TrainerSummaryRepository repository;

    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    void save_shouldPersistTrainerSummary() {
        TrainerSummary trainerSummary = createTrainerSummary("alice.smith", "Alice", "Smith");

        TrainerSummary saved = repository.save(trainerSummary);

        assertThat(saved.getId()).isNotNull();
        Optional<TrainerSummary> fetched = repository.findByUsername("alice.smith");
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void findByUsername_shouldReturnTrainerWithYearsAndMonths() {
        TrainerSummary trainerSummary = createTrainerSummary("bob.builder", "Bob", "Builder");
        repository.save(trainerSummary);

        Optional<TrainerSummary> fetchedOpt = repository.findByUsername("bob.builder");

        assertThat(fetchedOpt).isPresent();
        TrainerSummary fetched = fetchedOpt.get();
        assertThat(fetched.getYears()).hasSize(1);

        YearlySummary year = fetched.getYears().get(0);
        assertThat(year.getYearNumber()).isEqualTo(2025);
        assertThat(year.getMonths()).hasSize(2);

        MonthlySummary jan = year.getMonths().get(0);
        assertThat(jan.getMonthNumber()).isEqualTo(1);
        assertThat(jan.getTotalDurationMinutes()).isEqualTo(120);
    }

    @Test
    void updateMonthlyDuration_shouldPersistNewValue() {
        TrainerSummary trainerSummary = createTrainerSummary("carol.doe", "Carol", "Doe");
        repository.save(trainerSummary);

        TrainerSummary fetched = repository.findByUsername("carol.doe").orElseThrow();
        YearlySummary year = fetched.getYears().get(0);
        MonthlySummary jan = year.getMonths().get(0);
        jan.setTotalDurationMinutes(500);

        repository.save(fetched);

        TrainerSummary updated = repository.findByUsername("carol.doe").orElseThrow();
        assertThat(updated.getYears().get(0).getMonths().get(0).getTotalDurationMinutes())
                .isEqualTo(500);
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenTrainerNotExists() {
        Optional<TrainerSummary> fetched = repository.findByUsername("non.existing");

        assertThat(fetched).isEmpty();
    }

    private TrainerSummary createTrainerSummary(String username, String firstName, String lastName) {
        MonthlySummary january = new MonthlySummary();
        january.setMonthNumber(1);
        january.setTotalDurationMinutes(120);

        MonthlySummary february = new MonthlySummary();
        february.setMonthNumber(2);
        february.setTotalDurationMinutes(90);

        YearlySummary year2025 = new YearlySummary();
        year2025.setYearNumber(2025);
        year2025.setMonths(new ArrayList<>(List.of(january, february)));

        TrainerSummary trainer = new TrainerSummary();
        trainer.setUsername(username);
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        trainer.setActive(true);
        trainer.setYears(new ArrayList<>(List.of(year2025)));

        return trainer;
    }
}