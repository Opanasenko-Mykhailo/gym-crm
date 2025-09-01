package com.gcs.app.repository;

import com.gcs.app.model.TrainingType;
import com.github.database.rider.core.api.dataset.DataSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingTypeRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    @Test
    @DataSet(value = "dataset/training-type-data.xml", cleanBefore = true, cleanAfter = true)
    void findByName_existingType_returnsType() {
        Optional<TrainingType> result = trainingTypeRepository.findByName("Pilates");

        assertTrue(result.isPresent());
        assertEquals("Pilates", result.get().getName());
    }

    @Test
    @DataSet(value = "dataset/training-type-data.xml", cleanBefore = true, cleanAfter = true)
    void findByName_nonExistingType_returnsEmpty() {
        Optional<TrainingType> result = trainingTypeRepository.findByName("Boxing");

        assertFalse(result.isPresent());
    }

    @Test
    @DataSet(value = "dataset/training-type-data.xml", cleanBefore = true, cleanAfter = true)
    void findById_existingId_returnsType() {
        Optional<TrainingType> type = trainingTypeRepository.findById(1L);

        assertTrue(type.isPresent());
        assertEquals("Pilates", type.get().getName());
    }

    @Test
    void findAll_returnsAllTrainingTypes() {
        trainingTypeRepository.save(TrainingType.builder().name("Yoga").build());
        trainingTypeRepository.save(TrainingType.builder().name("HIIT").build());

        List<TrainingType> all = trainingTypeRepository.findAll();

        assertEquals(2, all.size());
    }
}