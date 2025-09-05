package com.gcc.app.repository;

import com.gcc.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcc.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcc.app.model.Training;
import jakarta.validation.Valid;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingQueryRepository {
    List<Training> findTrainingsForTrainee(@Valid TraineeTrainingSearchCriteriaDto criteria);
    List<Training> findTrainingsForTrainer(@Valid TrainerTrainingSearchCriteriaDto criteria);
}

