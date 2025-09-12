package com.example.bankcards.security.jwt;

import com.example.bankcards.config.JwtConfig;
import com.example.bankcards.dto.user.UserAuthResponse;
import com.example.bankcards.dto.user.UserMapper;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
public class JwtServiceImpl implements JwtService {
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;
    private final UserMapper mapper;
    private final JwtTokenFactory jwtTokenFactory;

    @Override
    public UserAuthResponse generateTokenPair(User user) {
        List<String> authorities = List.of(user.getRole().getAuthTitle());
        String accessToken = jwtTokenFactory.createAccessToken(user, authorities);
        String refreshToken = jwtTokenFactory.createRefreshToken(user);

        LocalDateTime expiry = LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTtl() / 1000);
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(expiry);
        userRepository.save(user);

        return new UserAuthResponse(
                accessToken,
                refreshToken,
                mapper.toDto(user)
        );
    }

    @Override
    public String extractAccessTokenFromRequest() {
        try {
            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();

            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }

            return null;
        } catch (IllegalStateException e) {
            log.warn("Request context not available");
            return null;
        }
    }
}