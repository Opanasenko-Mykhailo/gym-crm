package com.gcs.app.service;

import com.gcs.app.facade.dto.TrainingTypeResponseDto;
import com.gcs.app.model.TrainingType;

import java.util.List;

public interface TrainingTypeService {
    List<TrainingTypeResponseDto> getAll();

    TrainingType getByName(String trainingTypeName);
}
