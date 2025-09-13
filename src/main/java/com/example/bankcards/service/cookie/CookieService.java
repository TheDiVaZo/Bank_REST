package com.example.bankcards.service.cookie;

import jakarta.servlet.http.HttpServletResponse;

public interface CookieService {

    void setAccessToken(HttpServletResponse response, String accessToken);

    void setRefreshToken(HttpServletResponse response, String refreshToken);

    void removeAll(HttpServletResponse response);

    void remove(HttpServletResponse response, String name);
}
