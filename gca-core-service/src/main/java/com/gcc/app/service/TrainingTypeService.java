package com.gcc.app.service;

import com.gcc.app.facade.dto.TrainingTypeResponseDto;
import com.gcc.app.model.TrainingType;

import java.util.List;

public interface TrainingTypeService {
    List<TrainingTypeResponseDto> getAll();

    TrainingType getByName(String trainingTypeName);
}
