package com.gcm.repository;

import com.gcm.model.MonthlySummary;
import com.gcm.model.TrainerSummary;
import com.gcm.model.YearlySummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class TrainerSummaryRepositoryTest extends AbstractMongoRepositoryTest {

    @Autowired
    private TrainerSummaryRepository repository;

    @BeforeEach
    void setUp() {
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

        Iterator<YearlySummary> yearIterator = fetched.getYears().iterator();
        YearlySummary year = yearIterator.next();
        assertThat(year.getYearNumber()).isEqualTo(2025);
        assertThat(year.getMonths()).hasSize(2);

        Iterator<MonthlySummary> monthIterator = year.getMonths().iterator();
        MonthlySummary january = monthIterator.next();
        assertThat(january.getMonthNumber()).isEqualTo(1);
        assertThat(january.getTotalDurationMinutes()).isEqualTo(120);
    }

    @Test
    void updateMonthlyDuration_shouldPersistNewValue() {
        prepareTestDataForUpdate();

        TrainerSummary actual = repository.findByUsername("carol.doe").orElseThrow();
        Iterator<YearlySummary> yearIterator = actual.getYears().iterator();
        YearlySummary year = yearIterator.next();
        Iterator<MonthlySummary> monthIterator = year.getMonths().iterator();
        MonthlySummary january = monthIterator.next();
        january.setTotalDurationMinutes(500);
        repository.save(actual);

        TrainerSummary updated = repository.findByUsername("carol.doe").orElseThrow();
        Iterator<YearlySummary> updatedYearIterator = updated.getYears().iterator();
        Iterator<MonthlySummary> updatedMonthIterator = updatedYearIterator.next().getMonths().iterator();
        assertThat(updatedMonthIterator.next().getTotalDurationMinutes()).isEqualTo(500);
    }

    @Test
    void findByUsername_shouldReturnEmptyWhenTrainerNotExists() {
        Optional<TrainerSummary> actual = repository.findByUsername("non.existing");

        assertThat(actual).isEmpty();
    }

    private void prepareTestDataForUpdate() {
        TrainerSummary trainerSummary = createTrainerSummary("carol.doe", "Carol", "Doe");
        repository.save(trainerSummary);
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