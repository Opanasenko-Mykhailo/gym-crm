package com.gcs.app.facade.dto;

import com.gcs.app.model.TrainingType;
import lombok.Data;

@Data
public class TrainerResponseDto {
    private Long userId;
    private String firstName;
    private String lastName;
    private String username;
    private Boolean isActive;
    private TrainingType specialization;
}
