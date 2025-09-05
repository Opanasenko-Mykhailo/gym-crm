package com.gcc.app.service;

import com.gcc.app.facade.dto.TrainerCreateRequestDto;
import com.gcc.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcc.app.facade.dto.TrainerUpdateRequestDto;
import com.gcc.app.model.Trainee;
import com.gcc.app.model.Trainer;
import com.gcc.app.model.Training;
import jakarta.validation.Valid;

import java.util.List;

public interface TrainerService {
    Trainer createTrainer(@Valid TrainerCreateRequestDto trainerCreateRequestDto);

    Trainer updateTrainer(@Valid TrainerUpdateRequestDto trainerUpdateRequestDto);

    Trainer getByUsername(String username);

    List<Training> getTrainerTrainings(@Valid TrainerTrainingSearchCriteriaDto criteria);

    void setTrainerActivationStatus(String username, boolean isActive);

    List<Trainer> getUnassignedForTrainee(Trainee trainee);
}
