package com.mall.auth.service;

import com.mall.common.exception.BizException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "test-secret-key-for-unit-testing-only");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", 7200L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiry", 604800L);
    }

    @Test
    void generateAccessToken_returnsNonBlankToken() {
        String token = jwtService.generateAccessToken(1L, "user");
        assertThat(token).isNotBlank();
    }

    @Test
    void generateAccessToken_containsCorrectSubjectAndRole() {
        String token = jwtService.generateAccessToken(42L, "merchant");

        Long userId = jwtService.getUserIdFromToken(token);
        String role = jwtService.getRoleFromToken(token);

        assertThat(userId).isEqualTo(42L);
        assertThat(role).isEqualTo("merchant");
    }

    @Test
    void generateRefreshToken_returnsNonBlankToken() {
        String token = jwtService.generateRefreshToken(1L);
        assertThat(token).isNotBlank();
    }

    @Test
    void isRefreshToken_returnsTrueForRefreshToken() {
        String token = jwtService.generateRefreshToken(1L);
        assertThat(jwtService.isRefreshToken(token)).isTrue();
    }

    @Test
    void isRefreshToken_returnsFalseForAccessToken() {
        String token = jwtService.generateAccessToken(1L, "user");
        assertThat(jwtService.isRefreshToken(token)).isFalse();
    }

    @Test
    void validateToken_throwsBizExceptionForInvalidToken() {
        assertThatThrownBy(() -> jwtService.validateToken("invalid.token.here"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(401);
    }

    @Test
    void validateToken_throwsBizExceptionForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", -1L);
        String token = jwtService.generateAccessToken(1L, "user");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", 7200L);

        assertThatThrownBy(() -> jwtService.validateToken(token))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(401);
    }

    @Test
    void getUserIdFromToken_returnsCorrectUserId() {
        String token = jwtService.generateAccessToken(99L, "admin");
        assertThat(jwtService.getUserIdFromToken(token)).isEqualTo(99L);
    }

    @Test
    void getRemainingTtlSeconds_returnsPositiveValueForValidToken() {
        String token = jwtService.generateAccessToken(1L, "user");
        long ttl = jwtService.getRemainingTtlSeconds(token);
        assertThat(ttl).isGreaterThan(0).isLessThanOrEqualTo(7200L);
    }

    @Test
    void getAccessTokenExpiry_returnsConfiguredValue() {
        assertThat(jwtService.getAccessTokenExpiry()).isEqualTo(7200L);
    }

    @Test
    void getRefreshTokenExpiry_returnsConfiguredValue() {
        assertThat(jwtService.getRefreshTokenExpiry()).isEqualTo(604800L);
    }
}
