package com.gcs.app.controller;

import com.gcs.app.rest.ChangePasswordRequest;
import com.gcs.app.rest.LoginRequest;
import com.gcs.app.rest.LoginResponse;
import com.gcs.app.rest.RefreshTokenRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest extends AbstractControllerTest {

    private static final String USERNAME = "ivan.ivanov";
    private static final String PASSWORD = "Password!123";
    private static final String NEW_PASSWORD = "newPassword!456";

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        when(gymFacade.authenticate(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(true, "access-token", "refresh-token"));

        mockMvc.perform(post(basePath + "/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).authenticate(any(LoginRequest.class));
    }

    @Test
    void testRefreshTokenSuccess() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("dummy-refresh-token");

        when(gymFacade.refreshToken(any(RefreshTokenRequest.class)))
                .thenReturn(new LoginResponse(true, "new-access-token", "dummy-refresh-token"));

        mockMvc.perform(post(basePath + "/refresh-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void testLogoutSuccess() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("dummy-refresh-token");

        doNothing().when(gymFacade).logout(any(RefreshTokenRequest.class));

        mockMvc.perform(post(basePath + "/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).logout(any(RefreshTokenRequest.class));
    }

    @Test
    void testChangePasswordSuccess() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword(NEW_PASSWORD);

        doNothing().when(gymFacade).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put(basePath + "/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(gymFacade).changePassword(any(ChangePasswordRequest.class));
    }
}