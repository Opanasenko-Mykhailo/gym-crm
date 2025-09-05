package com.gcc.app.repository.impl;

import com.gcc.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcc.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcc.app.model.Training;
import com.gcc.app.repository.TrainingQueryRepository;
import com.gcc.app.repository.TrainingRepository;
import com.gcc.app.repository.specification.TraineeTrainingSpecification;
import com.gcc.app.repository.specification.TrainerTrainingSpecification;
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

