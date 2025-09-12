package com.example.bankcards.config;

import com.example.bankcards.security.jwt.JwtTokenFactoryImpl;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@RequiredArgsConstructor
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private long accessTtl;
    private long refreshTtl;

    @Bean
    public JwtTokenFactoryImpl jwtTokenFactory(SecretKey secretKey) {
        return new JwtTokenFactoryImpl(secretKey, accessTtl, refreshTtl);
    }

    @Bean
    public SecretKey secretKey() {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(secret.trim());
            return new SecretKeySpec(decodedKey, "HmacSHA256");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 JWT secret", e);
        }
    }
}

