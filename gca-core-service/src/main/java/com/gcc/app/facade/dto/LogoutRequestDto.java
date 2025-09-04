package com.gcc.app.facade.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequestDto {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}