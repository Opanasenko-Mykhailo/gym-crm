package com.gcs.app.repository.impl;

import com.gcs.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcs.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcs.app.model.Training;
import com.gcs.app.repository.TrainingQueryRepository;
import com.gcs.app.repository.TrainingRepository;
import com.gcs.app.repository.specification.TraineeTrainingSpecification;
import com.gcs.app.repository.specification.TrainerTrainingSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TrainingQueryRepositoryImpl implements TrainingQueryRepository {

    private final TrainingRepository trainingRepository;

    @Override
    public List<Training> findTrainingsForTrainee(TraineeTrainingSearchCriteriaDto criteria) {
        return trainingRepository.findAll(TraineeTrainingSpecification.findByCriteria(criteria));
    }

    @Override
    public List<Training> findTrainingsForTrainer(TrainerTrainingSearchCriteriaDto criteria) {
        return trainingRepository.findAll(TrainerTrainingSpecification.findByCriteria(criteria));
    }
}

