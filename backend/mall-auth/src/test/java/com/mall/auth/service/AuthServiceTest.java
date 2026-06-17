package com.mall.auth.service;

import com.mall.auth.dto.AuthResponse;
import com.mall.auth.dto.LoginRequest;
import com.mall.auth.dto.RegisterRequest;
import com.mall.auth.feign.UserFeignClient;
import com.mall.auth.feign.dto.CreateUserRequest;
import com.mall.auth.feign.dto.UserDTO;
import com.mall.common.exception.BizException;
import com.mall.common.result.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserFeignClient userFeignClient;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private AuthService authService;

    private UserDTO testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole("user");
        testUser.setPhone("13800138000");
        testUser.setStatus(1);
    }

    @Test
    void register_success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:verify:13800138000")).thenReturn("123456");
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userFeignClient.createUser(any(CreateUserRequest.class))).thenReturn(R.ok(testUser));
        when(jwtService.generateAccessToken(1L, "user")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiry()).thenReturn(7200L);
        when(jwtService.getRefreshTokenExpiry()).thenReturn(604800L);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPhone("13800138000");
        request.setPassword("password");
        request.setVerificationCode("123456");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(redisTemplate).delete("auth:verify:13800138000");
    }

    @Test
    void register_invalidVerificationCode_throwsBizException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:verify:13800138000")).thenReturn("654321");

        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138000");
        request.setVerificationCode("123456");

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(400);
    }

    @Test
    void login_withPasswordSuccess() {
        when(userFeignClient.findByAccount("testuser")).thenReturn(R.ok(testUser));
        testUser.setPasswordHash("encoded-hash");
        when(passwordEncoder.matches("password", "encoded-hash")).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(jwtService.generateAccessToken(1L, "user")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiry()).thenReturn(7200L);
        when(jwtService.getRefreshTokenExpiry()).thenReturn(604800L);

        LoginRequest request = new LoginRequest();
        request.setAccount("testuser");
        request.setPassword("password");
        request.setLoginType("password");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getUserId()).isEqualTo(1L);
    }

    @Test
    void login_invalidPassword_throwsBizException() {
        when(userFeignClient.findByAccount("testuser")).thenReturn(R.ok(testUser));
        testUser.setPasswordHash("encoded-hash");
        when(passwordEncoder.matches("wrong", "encoded-hash")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setAccount("testuser");
        request.setPassword("wrong");
        request.setLoginType("password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(401);
    }

    @Test
    void login_disabledAccount_throwsBizException() {
        testUser.setStatus(0);
        when(userFeignClient.findByAccount("testuser")).thenReturn(R.ok(testUser));

        LoginRequest request = new LoginRequest();
        request.setAccount("testuser");
        request.setPassword("password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(403);
    }

    @Test
    void login_userNotFound_throwsBizException() {
        when(userFeignClient.findByAccount("nouser")).thenReturn(R.fail(404, "Not found"));

        LoginRequest request = new LoginRequest();
        request.setAccount("nouser");
        request.setPassword("password");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(401);
    }

    @Test
    void login_smsCode_success() {
        when(userFeignClient.findByAccount("13800138000")).thenReturn(R.ok(testUser));
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:verify:13800138000")).thenReturn("123456");
        when(jwtService.generateAccessToken(1L, "user")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiry()).thenReturn(7200L);
        when(jwtService.getRefreshTokenExpiry()).thenReturn(604800L);

        LoginRequest request = new LoginRequest();
        request.setAccount("13800138000");
        request.setPassword("123456");
        request.setLoginType("sms_code");

        AuthResponse response = authService.login(request);
        assertThat(response.getToken()).isEqualTo("access-token");
    }

    @Test
    void refreshToken_success() {
        when(jwtService.isRefreshToken("old-refresh")).thenReturn(true);
        when(jwtService.getUserIdFromToken("old-refresh")).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:refresh:1")).thenReturn("old-refresh");
        when(valueOps.get("auth:session:1")).thenReturn("testuser:user");
        when(jwtService.generateAccessToken(1L, "user")).thenReturn("new-access");
        when(jwtService.generateRefreshToken(1L)).thenReturn("new-refresh");
        when(jwtService.getAccessTokenExpiry()).thenReturn(7200L);
        when(jwtService.getRefreshTokenExpiry()).thenReturn(604800L);

        AuthResponse response = authService.refreshToken("old-refresh");

        assertThat(response.getToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void refreshToken_invalidToken_throwsBizException() {
        when(jwtService.isRefreshToken("bad-token")).thenThrow(new BizException(401, "Invalid token"));

        assertThatThrownBy(() -> authService.refreshToken("bad-token"))
                .isInstanceOf(BizException.class);
    }

    @Test
    void refreshToken_expiredOrRevoked_throwsBizException() {
        when(jwtService.isRefreshToken("old-refresh")).thenReturn(true);
        when(jwtService.getUserIdFromToken("old-refresh")).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:refresh:1")).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshToken("old-refresh"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(401);
    }

    @Test
    void logout_success() {
        when(jwtService.getUserIdFromToken("token")).thenReturn(1L);
        when(jwtService.getRemainingTtlSeconds("token")).thenReturn(3600L);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        authService.logout("token");

        verify(redisTemplate).delete("auth:refresh:1");
        verify(redisTemplate).delete("auth:session:1");
    }

    @Test
    void logout_invalidToken_doesNotThrow() {
        when(jwtService.getUserIdFromToken("bad")).thenThrow(new BizException(401, "Invalid"));

        assertThatCode(() -> authService.logout("bad")).doesNotThrowAnyException();
    }
}
