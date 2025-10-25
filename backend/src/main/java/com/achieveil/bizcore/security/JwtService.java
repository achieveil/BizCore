package com.achieveil.bizcore.security;

import com.achieveil.bizcore.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties properties;

    private SecretKey signingKey() {
        String secret = properties.getSecret();
        if (!StringUtils.hasText(secret)) {
            byte[] buffer = new byte[64];
            SECURE_RANDOM.nextBytes(buffer);
            secret = Base64.getEncoder().encodeToString(buffer);
            LOGGER.warn("[JwtService] JWT secret is not configured. A temporary key was generated for this runtime instance.");
        }
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException ex) {
            keyBytes = secret.getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId, String username, String role, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("userId", userId);
        claims.put("sid", sessionId);

        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getAccessTokenTtl(), ChronoUnit.SECONDS)))
                .signWith(signingKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception ex) {
            return true;
        }
    }

    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    public String extractRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public Long extractUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public String extractSessionId(String token) {
        return parseToken(token).get("sid", String.class);
    }

    public long getAccessTokenTtlSeconds() {
        return properties.getAccessTokenTtl();
    }

    public long getRefreshTokenTtlSeconds() {
        return properties.getRefreshTokenTtl();
    }

    public String generateRefreshToken() {
        byte[] buffer = new byte[64];
        SECURE_RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }

}
