package com.gcc.app.service;

import com.gcc.app.facade.dto.TraineeCreateRequestDto;
import com.gcc.app.facade.dto.TraineeTrainingSearchCriteriaDto;
import com.gcc.app.facade.dto.TraineeUpdateRequestDto;
import com.gcc.app.model.Trainee;
import com.gcc.app.model.Trainer;
import com.gcc.app.model.Training;
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
