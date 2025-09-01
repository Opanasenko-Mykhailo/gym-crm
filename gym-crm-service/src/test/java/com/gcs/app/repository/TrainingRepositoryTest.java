package com.gcs.app.repository;

import com.gcs.app.model.Trainee;
import com.gcs.app.model.Trainer;
import com.gcs.app.model.Training;
import com.gcs.app.model.TrainingType;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DataSet(value = "dataset/training-data.xml", cleanBefore = true, cleanAfter = true)
    void findById_existingTraining_returnsTraining() {
        Optional<Training> foundTraining = trainingRepository.findById(4L);

        assertTrue(foundTraining.isPresent(), "Training with ID 4 should be found");
        assertEquals("Morning Yoga", foundTraining.get().getName());
        assertEquals(LocalDate.of(2025, 10, 1), foundTraining.get().getDate());
        assertEquals(60, foundTraining.get().getDuration());
        assertEquals(1L, foundTraining.get().getTrainee().getId());
        assertEquals(1L, foundTraining.get().getTrainer().getId());
        assertEquals(1L, foundTraining.get().getType().getId());
    }

    @Test
    @DataSet(value = "dataset/training-data.xml", cleanBefore = true, cleanAfter = true)
    void findById_nonExistingTraining_returnsEmpty() {
        Optional<Training> foundTraining = trainingRepository.findById(99L);

        assertFalse(foundTraining.isPresent(), "Training with ID 99 should not be found");
    }

    @Test
    @DataSet(value = "dataset/training-data.xml", cleanBefore = true, cleanAfter = true)
    void findAll_returnsAllTrainingsFromDataSet() {
        List<Training> trainings = trainingRepository.findAll();

        assertEquals(3, trainings.size(), "Should find 3 trainings from the dataset");

        assertTrue(trainings.stream().anyMatch(t -> "Morning Yoga".equals(t.getName())));
        assertTrue(trainings.stream().anyMatch(t -> "Evening Cardio".equals(t.getName())));
        assertTrue(trainings.stream().anyMatch(t -> "Strength Session".equals(t.getName())));
    }

    @Test
    @DataSet(cleanBefore = true, cleanAfter = true)
    void findAll_emptyDatabase_returnsEmptyList() {
        List<Training> trainings = trainingRepository.findAll();

        assertTrue(trainings.isEmpty(), "Should return an empty list when database is empty");
    }

    @Test
    @DataSet(value = "dataset/training-data.xml", cleanBefore = true, cleanAfter = true)
    void save_newTraining_persistsSuccessfully() {
        Trainee existingTrainee = traineeRepository.findById(1L).orElseThrow();
        Trainer existingTrainer = trainerRepository.findById(1L).orElseThrow();
        TrainingType existingTrainingType = trainingTypeRepository.findById(1L).orElseThrow();

        Training newTraining = Training.builder()
                .name("New Yoga Flow")
                .date(LocalDate.of(2025, 11, 15))
                .duration(75L)
                .trainee(existingTrainee)
                .trainer(existingTrainer)
                .type(existingTrainingType)
                .build();

        Training savedTraining = trainingRepository.save(newTraining);
        assertNotNull(savedTraining.getId(), "Saved training should have an ID");
        assertEquals("New Yoga Flow", savedTraining.getName());
        assertEquals(4, trainingRepository.findAll().size(), "Database should now contain 4 trainings");

        Optional<Training> found = trainingRepository.findById(savedTraining.getId());
        assertTrue(found.isPresent(), "Newly saved training should be retrievable by its ID");
        assertEquals("New Yoga Flow", found.get().getName());
    }
}