package com.example.bankcards.security.jwt;

import com.example.bankcards.dto.user.UserAuthResponse;
import com.example.bankcards.entity.User;

public interface JwtService {

    UserAuthResponse generateTokenPair(User user);

    String extractAccessTokenFromRequest();
}
