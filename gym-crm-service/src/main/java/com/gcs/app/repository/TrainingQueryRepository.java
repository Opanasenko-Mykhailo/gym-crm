package com.gcs.app.repository;

import com.gcs.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcs.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcs.app.model.Training;
import jakarta.validation.Valid;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingQueryRepository {
    List<Training> findTrainingsForTrainee(@Valid TraineeTrainingSearchCriteriaDto criteria);
    List<Training> findTrainingsForTrainer(@Valid TrainerTrainingSearchCriteriaDto criteria);
}

