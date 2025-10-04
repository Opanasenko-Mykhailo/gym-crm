package com.gcc.app.service.impl;

import com.gcc.app.exception.EntityNotFoundException;
import com.gcc.app.exception.ServiceException;
import com.gcc.app.facade.dto.PasswordChangeRequestDto;
import com.gcc.app.model.User;
import com.gcc.app.repository.UserRepository;
import com.gcc.app.service.common.CredentialsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String USERNAME = "lionel.venture";
    private static final String RAW_PASSWORD = "password123";
    private static final String NEW_PASSWORD = "NewPassword123!";

    @Mock
    private UserRepository userRepository;

    @Mock
    private CredentialsService credentialsService;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void getAllUsernames_returnsSetOfUsernames() {
        Set<String> expected = Set.of("lionel.venture", "jane.smith");

        when(userRepository.findAllUsernames()).thenReturn(expected);

        Set<String> actual = service.getAllUsernames();

        assertEquals(expected, actual);
        verify(userRepository).findAllUsernames();
    }

    @Test
    void changePassword_whenOldPasswordMatches_updatesPassword() {
        PasswordChangeRequestDto dto = new PasswordChangeRequestDto();
        dto.setUsername(USERNAME);
        dto.setOldPassword(RAW_PASSWORD);
        dto.setNewPassword(NEW_PASSWORD);

        String encodedOldPassword = "$2a$10$someEncodedOldPassword";
        String encodedNewPassword = "$2a$10$someEncodedNewPassword";

        User existingUser = User.builder()
                .username(USERNAME)
                .password(encodedOldPassword)
                .build();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));
        when(credentialsService.isPasswordCorrect(RAW_PASSWORD, encodedOldPassword)).thenReturn(true);
        when(credentialsService.encodePassword(NEW_PASSWORD)).thenReturn(encodedNewPassword);

        service.changePassword(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User updatedUser = captor.getValue();

        assertEquals(encodedNewPassword, updatedUser.getPassword());
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void changePassword_whenUserNotFound_throwsServiceException() {
        PasswordChangeRequestDto dto = new PasswordChangeRequestDto();
        dto.setUsername(USERNAME);
        dto.setOldPassword(RAW_PASSWORD);
        dto.setNewPassword(NEW_PASSWORD);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> service.changePassword(dto));

        assertEquals("User not found: " + USERNAME, ex.getMessage());
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void changePassword_whenOldPasswordIncorrect_throwsServiceException() {
        PasswordChangeRequestDto dto = new PasswordChangeRequestDto();
        dto.setUsername(USERNAME);
        dto.setOldPassword("wrongPassword");
        dto.setNewPassword(NEW_PASSWORD);

        String encodedOldPassword = "$2a$10$someEncodedOldPassword";

        User existingUser = User.builder()
                .username(USERNAME)
                .password(encodedOldPassword)
                .build();

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));
        when(credentialsService.isPasswordCorrect("wrongPassword", encodedOldPassword)).thenReturn(false);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.changePassword(dto));

        assertEquals("Old password is incorrect", ex.getMessage());
        verify(userRepository).findByUsername(USERNAME);
        verify(userRepository, never()).save(any());
    }
}