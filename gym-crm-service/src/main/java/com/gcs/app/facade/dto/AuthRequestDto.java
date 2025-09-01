package com.gcs.app.facade.dto;

import com.gcs.app.facade.constant.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequestDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(
            regexp = ValidationPatterns.PASSWORD_PATTERN,
            message = "Password must be at least 8 characters long, contain upper and lower case letters, a digit, and a special character"
    )
    private String password;
}
