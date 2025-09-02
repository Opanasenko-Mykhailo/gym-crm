package com.gcm.repository;

import com.gcm.model.TrainerSummary;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataSet("datasets/trainer_summary.yml")
class TrainerSummaryRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TrainerSummaryRepository repository;

    @Test
    void findAll_shouldReturnAllTrainerSummaries() {
        assertThat(repository.findAll()).isNotEmpty();
    }

    @Test
    void findByUsername_shouldReturnCorrectTrainer() {
        Optional<TrainerSummary> trainer = repository.findByUsername("alice.smith");
        assertThat(trainer).isPresent();
        assertThat(trainer.get().getFirstName()).isNotBlank();
    }

    @Test
    @DataSet(cleanBefore = true)
    void save_shouldPersistTrainerSummary() {
        TrainerSummary trainer = TrainerSummary.builder()
                .username("alice.wonder")
                .firstName("Alice")
                .lastName("Wonder")
                .active(true)
                .build();

        TrainerSummary saved = repository.save(trainer);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findByUsername("alice.wonder")).isPresent();
    }
}