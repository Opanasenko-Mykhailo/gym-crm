package com.gcs.app.facade.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TrainerTrainingSearchCriteriaDto extends TrainingSearchCriteria {

    @Size(max = 100, message = "Trainee name must be at most 100 characters")
    private String traineeName;
}
