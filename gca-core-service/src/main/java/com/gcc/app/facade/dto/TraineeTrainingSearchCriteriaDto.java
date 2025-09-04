package com.gcc.app.facade.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TraineeTrainingSearchCriteriaDto extends TrainingSearchCriteria {

    @Size(max = 100, message = "Trainer name must be at most 100 characters")
    private String trainerName;

    @Size(max = 100, message = "Training type name must be at most 100 characters")
    private String trainingTypeName;
}
