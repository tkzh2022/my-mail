package com.mall.auth.service;

import com.mall.auth.dto.AuthResponse;
import com.mall.auth.dto.LoginRequest;
import com.mall.auth.dto.RegisterRequest;
import com.mall.auth.feign.UserFeignClient;
import com.mall.auth.feign.dto.CreateUserRequest;
import com.mall.auth.feign.dto.UserDTO;
import com.mall.common.exception.BizException;
import com.mall.common.result.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final String SESSION_PREFIX = "auth:session:";
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String VERIFY_CODE_PREFIX = "auth:verify:";
    private static final String DEFAULT_ROLE = "user";
    private static final String SESSION_SEPARATOR = ":";

    private final JwtService jwtService;
    private final UserFeignClient userFeignClient;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        validateVerificationCode(request.getPhone(), request.getVerificationCode());

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(DEFAULT_ROLE)
                .build();

        R<UserDTO> response = userFeignClient.createUser(createUserRequest);
        UserDTO user = extractUser(response, 400, "Registration failed");

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        R<UserDTO> response = userFeignClient.findByAccount(request.getAccount());
        UserDTO user = extractUser(response, 401, "Invalid account or password");

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException(403, "Account is disabled");
        }

        if ("sms_code".equals(request.getLoginType())) {
            validateVerificationCode(user.getPhone(), request.getPassword());
        } else if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BizException(401, "Invalid account or password");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BizException(401, "Invalid refresh token");
        }

        Long userId = jwtService.getUserIdFromToken(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(refreshTokenKey(userId));
        if (!StringUtils.hasText(storedToken) || !storedToken.equals(refreshToken)) {
            throw new BizException(401, "Refresh token expired or revoked");
        }

        UserDTO user = loadSessionUser(userId);
        String role = StringUtils.hasText(user.getRole()) ? user.getRole() : DEFAULT_ROLE;
        String newAccessToken = jwtService.generateAccessToken(userId, role);
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        storeRefreshToken(userId, newRefreshToken);
        storeSession(user);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    public void logout(String token) {
        try {
            Long userId = jwtService.getUserIdFromToken(token);
            redisTemplate.delete(refreshTokenKey(userId));
            redisTemplate.delete(sessionKey(userId));

            long ttl = jwtService.getRemainingTtlSeconds(token);
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        blacklistKey(token),
                        "1",
                        Duration.ofSeconds(ttl)
                );
            }
        } catch (BizException e) {
            log.debug("Logout with invalid token ignored");
        }
    }

    private AuthResponse buildAuthResponse(UserDTO user) {
        String role = StringUtils.hasText(user.getRole()) ? user.getRole() : DEFAULT_ROLE;
        String accessToken = jwtService.generateAccessToken(user.getId(), role);
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        storeRefreshToken(user.getId(), refreshToken);
        storeSession(user);
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    private AuthResponse buildAuthResponse(UserDTO user, String accessToken, String refreshToken) {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setUserId(user.getId());
        authResponse.setUsername(user.getUsername());
        authResponse.setRole(StringUtils.hasText(user.getRole()) ? user.getRole() : DEFAULT_ROLE);
        authResponse.setToken(accessToken);
        authResponse.setRefreshToken(refreshToken);
        authResponse.setExpiresIn(jwtService.getAccessTokenExpiry());
        return authResponse;
    }

    private void storeRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                refreshTokenKey(userId),
                refreshToken,
                jwtService.getRefreshTokenExpiry(),
                TimeUnit.SECONDS
        );
    }

    private void storeSession(UserDTO user) {
        String sessionValue = user.getUsername() + SESSION_SEPARATOR +
                (StringUtils.hasText(user.getRole()) ? user.getRole() : DEFAULT_ROLE);
        redisTemplate.opsForValue().set(
                sessionKey(user.getId()),
                sessionValue,
                jwtService.getRefreshTokenExpiry(),
                TimeUnit.SECONDS
        );
    }

    private UserDTO loadSessionUser(Long userId) {
        String sessionValue = redisTemplate.opsForValue().get(sessionKey(userId));
        UserDTO user = new UserDTO();
        user.setId(userId);
        user.setRole(DEFAULT_ROLE);
        if (StringUtils.hasText(sessionValue)) {
            String[] parts = sessionValue.split(SESSION_SEPARATOR, 2);
            user.setUsername(parts[0]);
            if (parts.length > 1 && StringUtils.hasText(parts[1])) {
                user.setRole(parts[1]);
            }
        }
        return user;
    }

    private void validateVerificationCode(String phone, String verificationCode) {
        String storedCode = redisTemplate.opsForValue().get(verifyCodeKey(phone));
        if (!StringUtils.hasText(storedCode) || !storedCode.equals(verificationCode)) {
            throw new BizException(400, "Invalid verification code");
        }
        redisTemplate.delete(verifyCodeKey(phone));
    }

    private UserDTO extractUser(R<UserDTO> response, int code, String errorMessage) {
        if (response.getCode() != R.SUCCESS_CODE || response.getData() == null) {
            throw new BizException(code, errorMessage);
        }
        return response.getData();
    }

    private String refreshTokenKey(Long userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }

    private String sessionKey(Long userId) {
        return SESSION_PREFIX + userId;
    }

    private String blacklistKey(String token) {
        return TOKEN_BLACKLIST_PREFIX + token;
    }

    private String verifyCodeKey(String phone) {
        return VERIFY_CODE_PREFIX + phone;
    }
}
