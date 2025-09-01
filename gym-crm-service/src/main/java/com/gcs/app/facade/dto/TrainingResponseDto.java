package com.gcs.app.facade.dto;

import com.gcs.app.model.TrainingType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainingResponseDto {
    private Long id;
    private Long traineeId;
    private Long trainerId;
    private String name;
    private TrainingType type;
    private LocalDate date;
    private Long duration;
}
