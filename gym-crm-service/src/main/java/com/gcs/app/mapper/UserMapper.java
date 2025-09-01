package com.gcs.app.mapper;

import com.gcs.app.facade.dto.AuthRequestDto;
import com.gcs.app.facade.dto.AuthResponseDto;
import com.gcs.app.facade.dto.LogoutRequestDto;
import com.gcs.app.facade.dto.PasswordChangeRequestDto;
import com.gcs.app.facade.dto.RefreshTokenRequestDto;
import com.gcs.app.rest.ChangePasswordRequest;
import com.gcs.app.rest.LoginRequest;
import com.gcs.app.rest.LoginResponse;
import com.gcs.app.rest.RefreshTokenRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    AuthRequestDto toAuthRequestDto(LoginRequest loginRequest);

    LoginResponse toLoginResponse(AuthResponseDto authResponseDto);

    PasswordChangeRequestDto toPasswordChangeRequestDto(ChangePasswordRequest changePasswordRequest);

    RefreshTokenRequestDto toRefreshTokenRequestDto(RefreshTokenRequest request);

    LogoutRequestDto toLogoutRequestDto(RefreshTokenRequest request);
}