package com.example.bankcards.security.jwt;

import com.example.bankcards.entity.User;

public interface JwtTokenValidator {

    boolean isTokenValid(String token, User user);

    boolean isTokenRevoked(String token);

    void revokeToken(String token);
}