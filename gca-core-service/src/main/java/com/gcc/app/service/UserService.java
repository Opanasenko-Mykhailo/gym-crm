package com.gcc.app.service;

import com.gcc.app.facade.dto.PasswordChangeRequestDto;
import com.gcc.app.model.User;
import jakarta.validation.Valid;

import java.util.Set;

public interface UserService {
    User getByUsername(String username);

    Set<String> getAllUsernames();

    void changePassword(@Valid PasswordChangeRequestDto dto);
}
