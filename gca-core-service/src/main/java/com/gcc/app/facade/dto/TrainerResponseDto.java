package com.gcc.app.facade.dto;

import com.gcc.app.model.TrainingType;
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
