package com.example.bankcards.security.jwt;

import com.example.bankcards.exception.auth.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtParserImpl implements JwtParser {
    private final SecretKey secretKey;

    public JwtParserImpl(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parse(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public boolean isNotTokenExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    @Override
    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new InvalidTokenException("Incorrect JWT token: " + e.getMessage());
        }
    }
}
