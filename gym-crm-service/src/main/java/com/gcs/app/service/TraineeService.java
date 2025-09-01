package com.gcs.app.service;

import com.gcs.app.facade.dto.TraineeCreateRequestDto;
import com.gcs.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcs.app.facade.dto.TraineeUpdateRequestDto;
import com.gcs.app.model.Trainee;
import com.gcs.app.model.Trainer;
import com.gcs.app.model.Training;
import jakarta.validation.Valid;

import java.util.List;

public interface TraineeService {
    Trainee createTrainee(@Valid TraineeCreateRequestDto traineeCreateRequestDto);

    Trainee updateTrainee(@Valid TraineeUpdateRequestDto traineeUpdateRequestDto);

    void deleteTraineeByUsername(String username);

    Trainee getByUsername(String username);

    List<Training> getTraineeTrainings(@Valid TraineeTrainingSearchCriteriaDto criteria);

    void setTraineeActivationStatus(String username, boolean isActive);

    List<Trainer> getUnassignedTrainers(String traineeUsername);

    Trainee updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames);
}
