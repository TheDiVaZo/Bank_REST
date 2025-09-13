package com.example.bankcards.security.jwt;

import com.example.bankcards.exception.auth.InvalidTokenException;
import com.example.bankcards.exception.user.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final static String AUTH_HEADER = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";
    private final static String REFRESH_TOKEN_COOKIE = "__Host-refresh";
    private static final String AUTH_TOKEN_COOKIE = "__Host-auth-token";

    private final UserRepository userRepository;
    private final JwtParser jwtParser;
    private final JwtTokenValidator jwtTokenValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();

        // лог входящего запроса (коротко)
        log.debug("[JWT] --> {} {} from {} UA={}", method, path, request.getRemoteAddr(),
                request.getHeader("User-Agent"));

        if (request.getServletPath().startsWith("/auth/reg") ||
                request.getServletPath().startsWith("/auth/login") ||
                request.getServletPath().startsWith("/auth/validate") ||
                request.getServletPath().startsWith("/v3/api-docs") ||
                request.getServletPath().startsWith("/swagger-ui") ||
                request.getServletPath().startsWith("/actuator") ||
                request.getServletPath().startsWith("/docs") ||
                request.getServletPath().startsWith("/webjars/")) {
            log.trace("[JWT] Skipping filter for {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessToken = extractAccessToken(request);
            log.trace("[JWT] Access token present? {}", accessToken != null);
            if (accessToken == null) {
                throw new InvalidTokenException("Access token required!");
            }
            String username = jwtParser.extractUsername(accessToken);
            log.debug("[JWT] Username from token: {}", username);
            var user = userRepository.findByPhoneNumber(username)
                    .orElseThrow(() -> new UserNotFoundException("User for '"+username+"' not found"));
            boolean valid = jwtTokenValidator.isTokenValid(accessToken, user);
            log.debug("[JWT] Access token valid? {}", valid);
            if (!valid) {
                throw new InvalidTokenException("Invalid access token!");
            }

            if (request.getServletPath().equals("/auth/refresh")) {
                String refreshToken = extractRefreshToken(request);
                log.trace("[JWT] Refresh token present? {}", refreshToken != null);
                if (refreshToken == null) {
                    throw new InvalidTokenException("Refresh token required!");
                }
                if (!refreshToken.equals(user.getRefreshToken())) {
                    throw new InvalidTokenException("Invalid refresh token!");
                }
            }

            log.debug("[JWT] Auth OK -> principal={}, authorities={}", user.getUsername(), user.getAuthorities());

            var authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (InvalidTokenException e) {
            SecurityContextHolder.clearContext();
            log.warn("[JWT] Auth failed: {}", e.getMessage());
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        } finally {
            log.debug("[JWT] <-- {} {} -> {}",
                    method, path, response.getStatus());
        }
    }

    private String extractAccessToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (AUTH_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}