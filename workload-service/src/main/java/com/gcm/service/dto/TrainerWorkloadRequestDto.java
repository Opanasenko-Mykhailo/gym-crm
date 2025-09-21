package com.gcm.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
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

    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "First name must not be blank")
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    private String lastName;

    private boolean active;

    @NotNull(message = "Training date is required")
    @PastOrPresent(message = "Training date cannot be in the future")
    private LocalDate trainingDate;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than 0")
    private Long durationInMinutes;

    @NotNull(message = "Action type is required")
    private ActionType actionType;

    public enum ActionType {
        ADD,
        DELETE
    }
}