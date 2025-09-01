package com.gcs.app.service;

import com.gcs.app.facade.dto.TrainingCreateRequestDto;
import com.gcs.app.model.Training;
import jakarta.validation.Valid;

public interface TrainingService {
    Training createTraining(@Valid TrainingCreateRequestDto createRequestDto);

    Training getTraining(Long id);
}
