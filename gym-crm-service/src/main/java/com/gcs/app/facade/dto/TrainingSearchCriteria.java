package com.gcs.app.facade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public abstract class TrainingSearchCriteria {

    @NotBlank(message = "Username is required")
    @Size(max = 50, message = "Username must be at most 50 characters")
    private String username;

    @PastOrPresent(message = "From date must be in the past or present")
    private LocalDate fromDate;

    @PastOrPresent(message = "To date must be in the past or present")
    private LocalDate toDate;
}
