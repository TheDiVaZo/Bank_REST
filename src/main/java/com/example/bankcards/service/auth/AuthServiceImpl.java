package com.example.bankcards.service.auth;

import com.example.bankcards.dto.user.UserAuthResponse;
import com.example.bankcards.dto.user.UserLoginRequest;
import com.example.bankcards.dto.user.UserMapper;
import com.example.bankcards.dto.user.UserRegistrationRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.auth.InvalidPasswordException;
import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.exception.user.UserExistException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.jwt.JwtParser;
import com.example.bankcards.security.jwt.JwtService;
import com.example.bankcards.security.jwt.JwtTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;
    private final JwtParser jwtParser;
    private final JwtTokenValidator jwtTokenValidator;

    @Override
    @Transactional
    public UserAuthResponse registerUser(UserRegistrationRequest userRegistrationRequest) {
        if (userRepository.existsByPhoneNumber(userRegistrationRequest.getPhoneNumber())) {
            throw new UserExistException("User with this email or phone number already exists!");
        }
        User user = userMapper.fromUserRegistrationRequest(userRegistrationRequest);
        userRepository.save(user);
        userRepository.flush();
        return jwtService.generateTokenPair(user);
    }

    @Override
    public UserAuthResponse loginUser(UserLoginRequest userLoginRequest) {
        User user = userRepository.findByPhoneNumber(userLoginRequest.getPhoneNumber())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!passwordEncoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        return jwtService.generateTokenPair(user);
    }

    @Override
    public UserAuthResponse updateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new InvalidTokenException("Refresh token is empty!");
        }

        if (jwtTokenValidator.isTokenRevoked(refreshToken)) {
            throw new InvalidTokenException("Token revoked");
        }

        String phoneNumber = jwtParser.extractUsername(refreshToken);
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new InvalidTokenException("Refresh token does not match!");
        }

        if (!jwtTokenValidator.isTokenValid(refreshToken, user)) {
            if (user.getRefreshTokenExpiry() != null &&
                    user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new InvalidTokenException("The refresh token is expired!");
            }
            throw new InvalidTokenException("Refresh token is invalid!");
        }
        UserAuthResponse tokens = jwtService.generateTokenPair(user);
        jwtTokenValidator.revokeToken(refreshToken);
        return tokens;
    }

    @Override
    public void logout(String refreshToken) {
        userRepository.findByRefreshToken(refreshToken)
                .ifPresent(user -> {
                    String accessToken = jwtService.extractAccessTokenFromRequest();
                    if (accessToken != null) jwtTokenValidator.revokeToken(accessToken);
                    user.setRefreshToken(null);
                    userRepository.save(user);
                });
    }
}
