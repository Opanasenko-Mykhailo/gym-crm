package com.gcc.app.facade.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TraineeResponseDto {
    private Long userId;
    private String firstName;
    private String lastName;
    private String username;
    private Boolean isActive;
    private LocalDate dateOfBirth;
    private String address;
}
