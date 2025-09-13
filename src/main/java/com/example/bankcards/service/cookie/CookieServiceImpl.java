package com.example.bankcards.service.cookie;

import com.example.bankcards.config.JwtConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class CookieServiceImpl implements CookieService{
    private final JwtConfig jwtConfig;

    public CookieServiceImpl(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public void setAccessToken(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = ResponseCookie.from("__Host-auth-token", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge((int) jwtConfig.getAccessTtl() / 1000)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public void setRefreshToken(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("__Host-refresh", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge((int) jwtConfig.getRefreshTtl() / 1000)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public void removeAll(HttpServletResponse response) {
        remove(response, "__Host-auth-token");
        remove(response, "__Host-refresh");
    }

    @Override
    public void remove(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .maxAge(0)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
