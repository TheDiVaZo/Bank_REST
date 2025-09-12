package com.example.bankcards.security.jwt;

import com.example.bankcards.entity.User;

import java.util.List;

public interface JwtTokenFactory {

    String createRefreshToken(User user);

    String createAccessToken(User user, List<String> authorities);
}
