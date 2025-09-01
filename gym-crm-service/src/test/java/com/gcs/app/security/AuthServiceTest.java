package com.gcs.app.security;

import com.gcs.app.exception.InvalidCredentialsException;
import com.gcs.app.exception.ServiceException;
import com.gcs.app.exception.UserBlockedException;
import com.gcs.app.exception.UserNotAuthorizedException;
import com.gcs.app.facade.dto.AuthRequestDto;
import com.gcs.app.facade.dto.AuthResponseDto;
import com.gcs.app.facade.dto.LogoutRequestDto;
import com.gcs.app.facade.dto.RefreshTokenRequestDto;
import com.gcs.app.model.Role;
import com.gcs.app.model.User;
import com.gcs.app.model.enums.RoleType;
import com.gcs.app.service.UserService;
import com.gcs.app.service.common.CredentialsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String USERNAME = "rowan.atkinson";
    private static final String RAW_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "$2a$10$dummyhashhere";
    private static final String DUMMY_ACCESS_TOKEN = "dummy.access.token";
    private static final String DUMMY_REFRESH_TOKEN = "dummy.refresh.token";

    private final User activeUser = createUser();

    @Mock
    private UserService userService;

    @Mock
    private CredentialsService credentialsService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

    @InjectMocks
    private AuthService service;

    @Test
    void authenticate_whenCredentialsAreCorrect_returnsSuccessResponse() {
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername(USERNAME);
        dto.setPassword(RAW_PASSWORD);

        when(bruteForceProtectionService.isBlocked(USERNAME)).thenReturn(false);
        when(userService.getByUsername(USERNAME)).thenReturn(activeUser);
        when(credentialsService.isPasswordCorrect(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(eq(USERNAME), anySet())).thenReturn(DUMMY_ACCESS_TOKEN);
        when(jwtService.generateRefreshToken(USERNAME)).thenReturn(DUMMY_REFRESH_TOKEN);

        AuthResponseDto response = service.authenticate(dto);

        assertTrue(response.getSuccess());
        assertEquals(DUMMY_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(DUMMY_REFRESH_TOKEN, response.getRefreshToken());

        verify(bruteForceProtectionService).isBlocked(USERNAME);
        verify(userService).getByUsername(USERNAME);
        verify(credentialsService).isPasswordCorrect(RAW_PASSWORD, ENCODED_PASSWORD);
        verify(jwtService).generateToken(eq(USERNAME), anySet());
        verify(jwtService).generateRefreshToken(USERNAME);
        verify(refreshTokenService).saveToken(DUMMY_REFRESH_TOKEN, USERNAME);
        verify(bruteForceProtectionService).resetAttempts(USERNAME);
    }

    @Test
    void authenticate_whenUserIsInactive_throwsUserNotAuthorizedException() {
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername(USERNAME);
        dto.setPassword(RAW_PASSWORD);

        User inactiveUser = User.builder()
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .isActive(false)
                .build();

        when(bruteForceProtectionService.isBlocked(USERNAME)).thenReturn(false);
        when(userService.getByUsername(USERNAME)).thenReturn(inactiveUser);

        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,
                () -> service.authenticate(dto));

        assertEquals("User is inactive", ex.getMessage());
        verify(bruteForceProtectionService).isBlocked(USERNAME);
        verify(userService).getByUsername(USERNAME);
        verifyNoMoreInteractions(credentialsService, jwtService, refreshTokenService);
    }

    @Test
    void authenticate_whenUserNotFound_throwsServiceException() {
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername(USERNAME);
        dto.setPassword(RAW_PASSWORD);

        when(bruteForceProtectionService.isBlocked(USERNAME)).thenReturn(false);
        when(userService.getByUsername(USERNAME)).thenThrow(new ServiceException("User not found: " + USERNAME));

        ServiceException ex = assertThrows(ServiceException.class, () -> service.authenticate(dto));

        assertEquals("User not found: " + USERNAME, ex.getMessage());
        verify(bruteForceProtectionService).isBlocked(USERNAME);
        verify(userService).getByUsername(USERNAME);
        verifyNoMoreInteractions(credentialsService, jwtService, refreshTokenService);
    }

    @Test
    void authenticate_whenUserIsBlocked_throwsUserBlockedException() {
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername(USERNAME);
        dto.setPassword(RAW_PASSWORD);

        when(bruteForceProtectionService.isBlocked(USERNAME)).thenReturn(true);

        UserBlockedException ex = assertThrows(UserBlockedException.class,
                () -> service.authenticate(dto));

        assertEquals("Too many failed login attempts, try again later", ex.getMessage());
        verify(bruteForceProtectionService).isBlocked(USERNAME);
        verifyNoMoreInteractions(userService, credentialsService, jwtService, refreshTokenService);
    }

    @Test
    void authenticate_whenPasswordIncorrect_throwsInvalidCredentialsException() {
        AuthRequestDto dto = new AuthRequestDto();
        dto.setUsername(USERNAME);
        dto.setPassword("wrongPassword");

        when(bruteForceProtectionService.isBlocked(USERNAME)).thenReturn(false);
        when(userService.getByUsername(USERNAME)).thenReturn(activeUser);
        when(credentialsService.isPasswordCorrect("wrongPassword", ENCODED_PASSWORD)).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> service.authenticate(dto));

        assertEquals("Invalid username or password", ex.getMessage());
        verify(bruteForceProtectionService).isBlocked(USERNAME);
        verify(userService).getByUsername(USERNAME);
        verify(credentialsService).isPasswordCorrect("wrongPassword", ENCODED_PASSWORD);
        verify(bruteForceProtectionService).recordFailedAttempt(USERNAME);
        verifyNoMoreInteractions(jwtService, refreshTokenService);
    }

    @Test
    void refreshToken_whenValidToken_returnsNewAccessToken() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken(DUMMY_REFRESH_TOKEN);

        when(jwtService.isTokenValid(DUMMY_REFRESH_TOKEN)).thenReturn(true);
        when(refreshTokenService.getUsername(DUMMY_REFRESH_TOKEN)).thenReturn(USERNAME);
        when(jwtService.extractUsername(DUMMY_REFRESH_TOKEN)).thenReturn(USERNAME);
        when(userService.getByUsername(USERNAME)).thenReturn(activeUser);
        when(jwtService.generateToken(eq(USERNAME), anySet())).thenReturn(DUMMY_ACCESS_TOKEN);

        AuthResponseDto response = service.refreshToken(request);

        assertTrue(response.getSuccess());
        assertEquals(DUMMY_ACCESS_TOKEN, response.getAccessToken());
        assertEquals(DUMMY_REFRESH_TOKEN, response.getRefreshToken());

        verify(jwtService).isTokenValid(DUMMY_REFRESH_TOKEN);
        verify(refreshTokenService).getUsername(DUMMY_REFRESH_TOKEN);
        verify(jwtService).extractUsername(DUMMY_REFRESH_TOKEN);
        verify(userService).getByUsername(USERNAME);
        verify(jwtService).generateToken(eq(USERNAME), anySet());
    }

    @Test
    void refreshToken_whenInvalidToken_throwsUserNotAuthorizedException() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken(DUMMY_REFRESH_TOKEN);

        when(jwtService.isTokenValid(DUMMY_REFRESH_TOKEN)).thenReturn(false);

        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,
                () -> service.refreshToken(request));

        assertEquals("Invalid or expired refresh token", ex.getMessage());
        verify(jwtService).isTokenValid(DUMMY_REFRESH_TOKEN);
        verifyNoMoreInteractions(refreshTokenService, userService, jwtService);
    }

    @Test
    void refreshToken_whenUsernameMismatch_throwsUserNotAuthorizedException() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken(DUMMY_REFRESH_TOKEN);

        when(jwtService.isTokenValid(DUMMY_REFRESH_TOKEN)).thenReturn(true);
        when(refreshTokenService.getUsername(DUMMY_REFRESH_TOKEN)).thenReturn(USERNAME);
        when(jwtService.extractUsername(DUMMY_REFRESH_TOKEN)).thenReturn("anotherUser");

        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,
                () -> service.refreshToken(request));

        assertEquals("Invalid refresh token", ex.getMessage());
        verify(jwtService).isTokenValid(DUMMY_REFRESH_TOKEN);
        verify(refreshTokenService).getUsername(DUMMY_REFRESH_TOKEN);
        verify(jwtService).extractUsername(DUMMY_REFRESH_TOKEN);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void logout_whenValidToken_invokesInvalidate() {
        LogoutRequestDto request = new LogoutRequestDto();
        request.setRefreshToken(DUMMY_REFRESH_TOKEN);

        when(jwtService.isTokenValid(DUMMY_REFRESH_TOKEN)).thenReturn(true);
        when(refreshTokenService.getUsername(DUMMY_REFRESH_TOKEN)).thenReturn(USERNAME);
        when(jwtService.extractUsername(DUMMY_REFRESH_TOKEN)).thenReturn(USERNAME);

        service.logout(request);

        verify(jwtService).isTokenValid(DUMMY_REFRESH_TOKEN);
        verify(refreshTokenService).getUsername(DUMMY_REFRESH_TOKEN);
        verify(jwtService).extractUsername(DUMMY_REFRESH_TOKEN);
        verify(refreshTokenService).invalidateToken(DUMMY_REFRESH_TOKEN);
    }

    @Test
    void logout_whenInvalidToken_throwsUserNotAuthorizedException() {
        LogoutRequestDto request = new LogoutRequestDto();
        request.setRefreshToken(DUMMY_REFRESH_TOKEN);

        when(jwtService.isTokenValid(DUMMY_REFRESH_TOKEN)).thenReturn(false);

        UserNotAuthorizedException ex = assertThrows(UserNotAuthorizedException.class,
                () -> service.logout(request));

        assertEquals("Invalid or expired refresh token", ex.getMessage());
        verify(jwtService).isTokenValid(DUMMY_REFRESH_TOKEN);
        verifyNoMoreInteractions(refreshTokenService);
    }

    private User createUser() {
        return User.builder()
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .isActive(true)
                .roles(Set.of(new Role(1L, RoleType.ROLE_TRAINEE)))
                .build();
    }
}