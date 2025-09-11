package com.gcc.app.service.impl;

import com.gcc.app.exception.ServiceException;
import com.gcc.app.facade.dto.TrainingCreateRequestDto;
import com.gcc.app.mapper.TrainingMapper;
import com.gcc.app.model.Training;
import com.gcc.app.repository.TrainingRepository;
import com.gcc.app.service.TraineeService;
import com.gcc.app.service.TrainerService;
import com.gcc.app.service.TrainingService;
import com.gcc.app.service.TrainingTypeService;
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

    @Override
    @Transactional
    public void deleteById(Long id) {
        log.info("Deleting Training by id: {}", id);

        Training training = trainingRepository.findById(id)
                .orElseThrow(() -> new ServiceException(String.format("Training not found with id: %d", id)));

        trainingRepository.delete(training);
        log.info("Deleted Training with id: {}", id);
    }

    private Optional<Training> validateTrainingExists(Long id) {
        Optional<Training> training = trainingRepository.findById(id);

        if (training.isEmpty()) {
            throw new ServiceException(String.format("Training with id %d not found", id));
        }

        return training;
    }
}