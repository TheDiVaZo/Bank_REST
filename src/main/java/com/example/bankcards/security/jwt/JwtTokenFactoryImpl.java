package com.example.bankcards.security.jwt;

import com.example.bankcards.entity.User;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class JwtTokenFactoryImpl implements JwtTokenFactory {
    private final SecretKey secretKey;
    private final long accessTtl;
    private final long refreshTtl;

    @Override
    public String createRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getPhoneNumber())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(refreshTtl)))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String createAccessToken(User user, List<String> authorities) {
        return Jwts.builder()
                .subject(user.getPhoneNumber())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusMillis(accessTtl)))
                .claim("authorities", authorities)
                .signWith(secretKey)
                .compact();
    }
}
