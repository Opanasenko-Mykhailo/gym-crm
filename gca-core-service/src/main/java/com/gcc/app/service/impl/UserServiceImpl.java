package com.gcc.app.service.impl;

import com.gcc.app.exception.ServiceException;
import com.gcc.app.facade.dto.PasswordChangeRequestDto;
import com.gcc.app.model.User;
import com.gcc.app.repository.UserRepository;
import com.gcc.app.service.UserService;
import com.gcc.app.service.common.CredentialsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Validated
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CredentialsService credentialsService;

    @Transactional(readOnly = true)
    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ServiceException("User not found: " + username));
    }

    @Transactional(readOnly = true)
    @Override
    public Set<String> getAllUsernames() {
        return userRepository.findAllUsernames();
    }

    @Transactional
    @Override
    public void changePassword(@Valid PasswordChangeRequestDto dto) {
        log.info("Changing password for username: {}", dto.getUsername());

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ServiceException("User not found: " + dto.getUsername()));

        if (!credentialsService.isPasswordCorrect(dto.getOldPassword(), user.getPassword())) {
            throw new ServiceException("Old password is incorrect");
        }

        String encodedNewPassword = credentialsService.encodePassword(dto.getNewPassword());

        User updatedUser = user.toBuilder()
                .password(encodedNewPassword)
                .build();

        userRepository.save(updatedUser);
        log.info("Password changed successfully for username: {}", dto.getUsername());
    }
}
