package com.gcc.app.service;

import com.gcc.app.facade.dto.TrainingCreateRequestDto;
import com.gcc.app.model.Training;
import jakarta.validation.Valid;

public interface TrainingService {
    Training createTraining(@Valid TrainingCreateRequestDto createRequestDto);

    Training getTraining(Long id);

    void deleteById(Long id);
}
