package com.gcm.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TrainerWorkloadRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private boolean isActive;

    @NotNull
    private LocalDate trainingDate;

    @Min(1)
    private int durationInMinutes;

    @Pattern(regexp = "ADD|DELETE", message = "ActionType must be ADD or DELETE")
    private String actionType;
}