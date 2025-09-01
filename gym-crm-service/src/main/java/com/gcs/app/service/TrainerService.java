package com.gcs.app.service;

import com.gcs.app.facade.dto.TrainerCreateRequestDto;
import com.gcs.app.facade.dto.TrainerTrainingSearchCriteriaDto;
import com.gcs.app.facade.dto.TrainerUpdateRequestDto;
import com.gcs.app.model.Trainee;
import com.gcs.app.model.Trainer;
import com.gcs.app.model.Training;
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
