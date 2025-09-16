package com.gcc.app.integration.workload.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadRequestDto {
    private String username;
    private String firstName;
    private String lastName;
    private boolean active;
    private LocalDate trainingDate;
    private Long durationInMinutes;
    private ActionType actionType;

    public enum ActionType {
        ADD,
        DELETE
    }
}