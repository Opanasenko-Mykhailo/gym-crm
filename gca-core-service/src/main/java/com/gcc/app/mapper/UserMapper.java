package com.gcc.app.mapper;

import com.gcc.app.facade.dto.AuthRequestDto;
import com.gcc.app.facade.dto.AuthResponseDto;
import com.gcc.app.facade.dto.LogoutRequestDto;
import com.gcc.app.facade.dto.PasswordChangeRequestDto;
import com.gcc.app.facade.dto.RefreshTokenRequestDto;
import com.gcc.app.rest.ChangePasswordRequest;
import com.gcc.app.rest.LoginRequest;
import com.gcc.app.rest.LoginResponse;
import com.gcc.app.rest.RefreshTokenRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    AuthRequestDto toAuthRequestDto(LoginRequest loginRequest);

    LoginResponse toLoginResponse(AuthResponseDto authResponseDto);

    PasswordChangeRequestDto toPasswordChangeRequestDto(ChangePasswordRequest changePasswordRequest);

    RefreshTokenRequestDto toRefreshTokenRequestDto(RefreshTokenRequest request);

    LogoutRequestDto toLogoutRequestDto(RefreshTokenRequest request);
}