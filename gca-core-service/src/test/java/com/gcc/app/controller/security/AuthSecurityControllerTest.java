package com.gcc.app.controller.security;

import com.gcc.app.rest.ChangePasswordRequest;
import com.gcc.app.rest.LoginRequest;
import com.gcc.app.rest.RefreshTokenRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthSecurityControllerTest extends AbstractSecurityControllerTest {

    private static final String USERNAME = "ivan.ivanov";
    private static final String PASSWORD = "Password!123";
    private static final String NEW_PASSWORD = "newPassword!456";

    @Test
    @WithAnonymousUser
    void login_shouldBeAccessibleWithoutAuth() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);

        mockMvc.perform(post(basePath + "/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void refreshToken_shouldBeAccessibleWithoutAuth() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("dummy-refresh-token");

        mockMvc.perform(post(basePath + "/refresh-token")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void logout_shouldBeAccessibleWithoutAuth() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("dummy-refresh-token");

        mockMvc.perform(post(basePath + "/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void changePassword_shouldReturnOkForAuthenticatedUser() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUsername(USERNAME);
        request.setOldPassword(PASSWORD);
        request.setNewPassword(NEW_PASSWORD);

        mockMvc.perform(put(basePath + "/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}