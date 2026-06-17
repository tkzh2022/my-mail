package com.mall.auth.service;

import com.mall.common.exception.BizException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry:7200}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry:604800}")
    private long refreshTokenExpiry;

    public String generateAccessToken(Long userId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_TYPE, TOKEN_TYPE_ACCESS)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TYPE, TOKEN_TYPE_REFRESH)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiry * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException(401, "Invalid token");
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.parseLong(validateToken(token).getSubject());
    }

    public String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(CLAIM_ROLE, String.class);
    }

    public boolean isRefreshToken(String token) {
        Claims claims = validateToken(token);
        return TOKEN_TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public long getAccessTokenExpiry() {
        return accessTokenExpiry;
    }

    public long getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }

    public long getRemainingTtlSeconds(String token) {
        Claims claims = validateToken(token);
        Date expiration = claims.getExpiration();
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();
        return Math.max(remainingMillis / 1000, 0);
    }

    private SecretKey getSigningKey() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
