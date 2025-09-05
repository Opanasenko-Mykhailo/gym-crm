package com.gcc.app.facade.dto;

import com.gcc.app.facade.constant.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequestDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Old password is required")
    @Size(min = 8, max = 255, message = "Old password must be between 8 and 255 characters")
    @Pattern(
            regexp = ValidationPatterns.PASSWORD_PATTERN,
            message = "Old password must be at least 8 characters long, contain upper and lower case letters, a digit, and a special character"
    )
    private String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 255, message = "New password must be between 8 and 255 characters")
    @Pattern(
            regexp = ValidationPatterns.PASSWORD_PATTERN,
            message = "New password must be at least 8 characters long, contain upper and lower case letters, a digit, and a special character"
    )
    private String newPassword;
}
