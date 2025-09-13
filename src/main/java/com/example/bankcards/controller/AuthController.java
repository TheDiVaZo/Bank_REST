package com.example.bankcards.controller;

import com.example.bankcards.dto.user.UserAuthResponse;
import com.example.bankcards.dto.user.UserLoginRequest;
import com.example.bankcards.dto.user.UserRegistrationRequest;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.service.auth.AuthService;
import com.example.bankcards.service.cookie.CookieService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final CookieService cookieService;
    private final AuthService authService;

    @PostMapping("/reg")
    public UserAuthResponse registrationUser(
            @RequestBody UserRegistrationRequest userRegistrationRequest,
            HttpServletResponse response
    ) {
        UserAuthResponse loginResponse = authService.registrationUser(userRegistrationRequest);
        cookieService.setRefreshToken(response, loginResponse.getRefreshToken());
        return loginResponse;
    }

    @PostMapping("/login")
    public UserAuthResponse loginUser(
            @RequestBody UserLoginRequest userLoginRequest,
            HttpServletResponse response
    ) {
        UserAuthResponse loginResponse = authService.loginUser(userLoginRequest);
        cookieService.setRefreshToken(response, loginResponse.getRefreshToken());
        return loginResponse;
    }

    @PostMapping("/refresh")
    public UserAuthResponse refresh(
            @CookieValue(value = "__Host-refresh", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            cookieService.removeAll(response);
            throw new InvalidTokenException("Refresh token is not get");
        }
        try {
            UserAuthResponse tokens = authService.updateRefreshToken(refreshToken);

            cookieService.setAccessToken(response, tokens.getAccessToken());
            cookieService.setRefreshToken(response, tokens.getRefreshToken());

            return tokens;
        } catch (InvalidTokenException e) {
            cookieService.removeAll(response);
            throw e;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue("__Host-refresh") String refreshToken,
            HttpServletResponse response) {

        authService.logout(refreshToken);
        cookieService.removeAll(response);
        return ResponseEntity.ok().build();
    }
}
