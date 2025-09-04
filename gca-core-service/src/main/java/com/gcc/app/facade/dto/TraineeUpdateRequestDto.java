package com.gcc.app.facade.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TraineeUpdateRequestDto {
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    private Boolean isActive;

    @PastOrPresent(message = "Date of birth must be in the past or today")
    private LocalDate dateOfBirth;

    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;
}
