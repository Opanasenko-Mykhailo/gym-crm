package com.gcm.repository;

import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.YearlySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
class TrainerSummaryRepositoryTest {
    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:7.0.5");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }

    @Autowired
    private TrainerSummaryRepository repository;

    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    void save_shouldPersistTrainerSummary() {
        TrainerSummary trainerSummary = createTrainerSummary("alice.smith", "Alice", "Smith");

        TrainerSummary actual = repository.save(trainerSummary);

        assertThat(actual.getId()).isNotNull();
        Optional<TrainerSummary> fetched = repository.findByUsername("alice.smith");
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getFirstName()).isEqualTo("Alice");
    }

    @Test
    void findByUsername_shouldReturnTrainerWithYearsAndMonths() {
        TrainerSummary trainerSummary = createTrainerSummary("bob.builder", "Bob", "Builder");
        repository.save(trainerSummary);

        Optional<TrainerSummary> actual = repository.findByUsername("bob.builder");

        assertThat(actual).isPresent();
        TrainerSummary fetched = actual.get();
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

        TrainerSummary actual = repository.findByUsername("carol.doe").orElseThrow();
        YearlySummary year = actual.getYears().get(0);
        MonthlySummary jan = year.getMonths().get(0);
        jan.setTotalDurationMinutes(500);

        repository.save(actual);

        TrainerSummary updated = repository.findByUsername("carol.doe").orElseThrow();
        assertThat(updated.getYears().get(0).getMonths().get(0).getTotalDurationMinutes())
                .isEqualTo(500);
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenTrainerNotExists() {
        Optional<TrainerSummary> actual = repository.findByUsername("non.existing");

        assertThat(actual).isEmpty();
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