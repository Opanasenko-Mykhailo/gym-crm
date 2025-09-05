package com.gcc.app.repository;

import com.gcc.app.model.Trainee;
import com.gcc.app.model.Trainer;
import com.gcc.app.model.TrainingType;
import com.gcc.app.model.User;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerRepositoryTest extends AbstractRepositoryTest {

    private static final String EXISTING_USERNAME = "jane.smith";
    private static final String NEW_USERNAME = "alex.ivanov";

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DataSet(value = "dataset/trainer-data.xml", cleanBefore = true, cleanAfter = true)
    void findByUsername_existingUsername_returnsTrainer() {
        Optional<Trainer> trainer = trainerRepository.findByUsername(EXISTING_USERNAME);

        assertTrue(trainer.isPresent());
        assertEquals(EXISTING_USERNAME, trainer.get().getUser().getUsername());
        assertNotNull(trainer.get().getTrainees());
        assertFalse(trainer.get().getTrainees().isEmpty());
    }

    @Test
    @DataSet(value = "dataset/trainer-data.xml", cleanBefore = true, cleanAfter = true)
    void findByUsername_nonExistingUsername_returnsEmpty() {
        Optional<Trainer> trainer = trainerRepository.findByUsername("nonexistent");
        assertFalse(trainer.isPresent());
    }

    @Test
    @DataSet(value = "dataset/trainer-data.xml", cleanBefore = true, cleanAfter = true)
    void findAllNotAssignedToTrainee_returnsCorrectTrainers() {
        Trainee trainee = traineeRepository.findByUsername("sofia.melnyk")
                .orElseThrow(() -> new RuntimeException("Trainee not found"));

        List<Trainer> trainers = trainerRepository.findAllNotAssignedToTrainee(trainee);

        assertNotNull(trainers);
        assertTrue(trainers.stream().noneMatch(t -> t.getTrainees().contains(trainee)));
    }

    @Test
    @DataSet(value = "dataset/trainer-data.xml", cleanBefore = true, cleanAfter = true)
    void createTrainer_savesSuccessfully() {
        User user = User.builder()
                .username(NEW_USERNAME)
                .password("pass")
                .firstName("Alex")
                .lastName("Ivanov")
                .isActive(true)
                .build();

        TrainingType specialization = findTrainingType("Yoga");

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(specialization)
                .build();

        Trainer saved = trainerRepository.save(trainer);
        assertNotNull(saved.getId());
        assertEquals(NEW_USERNAME, saved.getUser().getUsername());
    }

    @Test
    @DataSet(value = "dataset/trainer-data.xml", cleanBefore = true, cleanAfter = true)
    void updateTrainer_specializationUpdatedSuccessfully() {
        Trainer existingTrainer = trainerRepository.findByUsername(EXISTING_USERNAME)
                .orElseThrow(() -> new RuntimeException("Trainer not found: " + EXISTING_USERNAME));
        TrainingType newType = findTrainingType("Cardio");

        Trainer updated = existingTrainer.toBuilder().specialization(newType).build();
        Trainer saved = trainerRepository.save(updated);

        assertEquals("Cardio", saved.getSpecialization().getName());
    }

    @Test
    @DataSet(value = "dataset/trainer-data.xml", cleanBefore = true, cleanAfter = true)
    void deleteTrainer_removedFromDatabase() {
        Trainer trainer = trainerRepository.findByUsername(EXISTING_USERNAME)
                .orElseThrow(() -> new RuntimeException("Trainer not found: " + EXISTING_USERNAME));
        trainerRepository.delete(trainer);

        Optional<Trainer> deleted = trainerRepository.findById(trainer.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    @DataSet(value = "dataset/trainer-data.xml", cleanBefore = true, cleanAfter = true)
    void findAll_returnsAllTrainers() {
        List<Trainer> trainers = trainerRepository.findAll();

        assertNotNull(trainers);
        assertEquals(2, trainers.size());
    }

    private TrainingType findTrainingType(String name) {
        return trainingTypeRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("TrainingType not found: " + name));
    }
}