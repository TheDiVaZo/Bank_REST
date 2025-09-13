package com.example.bankcards.service.auth;

import com.example.bankcards.dto.user.UserAuthResponse;
import com.example.bankcards.dto.user.UserLoginRequest;
import com.example.bankcards.dto.user.UserRegistrationRequest;

public interface AuthService {

    UserAuthResponse registrationUser(UserRegistrationRequest userRegistrationRequest);

    UserAuthResponse loginUser(UserLoginRequest userLoginRequest);

    UserAuthResponse updateRefreshToken(String refreshToken);

    void logout(String refreshToken);
}
