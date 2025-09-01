package com.gcs.app.service.impl;

import com.gcs.app.exception.ServiceException;
import com.gcs.app.facade.dto.TrainingCreateRequestDto;
import com.gcs.app.mapper.TrainingMapper;
import com.gcs.app.model.Training;
import com.gcs.app.repository.TrainingRepository;
import com.gcs.app.service.TraineeService;
import com.gcs.app.service.TrainerService;
import com.gcs.app.service.TrainingService;
import com.gcs.app.service.TrainingTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TrainingMapper trainingMapper;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingTypeService trainingTypeService;

    @Transactional
    @Override
    public Training createTraining(@Valid TrainingCreateRequestDto createRequestDto) {
        var type = trainingTypeService.getByName(createRequestDto.getType().getName());
        var trainee = traineeService.getByUsername(createRequestDto.getTraineeUsername());
        var trainer = trainerService.getByUsername(createRequestDto.getTrainerUsername());

        Training training = trainingMapper.toEntity(createRequestDto);

        training = training.toBuilder()
                .type(type)
                .trainee(trainee)
                .trainer(trainer)
                .build();

        log.info("Creating training: {}", training.getName());

        return trainingRepository.save(training);
    }

    @Transactional(readOnly = true)
    @Override
    public Training getTraining(Long id) {
        log.info("Retrieving training with id: {}", id);

        Training training = validateTrainingExists(id).orElseThrow(() -> new ServiceException(String.format("Training with id %d not found", id)));
        log.info("Training retrieved: {}", training);

        return training;
    }

    private Optional<Training> validateTrainingExists(Long id) {
        Optional<Training> training = trainingRepository.findById(id);

        if (training.isEmpty()) {
            throw new ServiceException(String.format("Training with id %d not found", id));
        }

        return training;
    }
}