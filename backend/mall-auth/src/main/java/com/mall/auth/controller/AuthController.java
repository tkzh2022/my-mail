package com.mall.auth.controller;

import com.mall.auth.dto.AuthResponse;
import com.mall.auth.dto.LoginRequest;
import com.mall.auth.dto.RefreshTokenRequest;
import com.mall.auth.dto.RegisterRequest;
import com.mall.auth.service.AuthService;
import com.mall.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public R<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return R.ok(authService.register(request));
    }

    @PostMapping("/login")
    public R<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public R<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return R.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (StringUtils.hasText(authorization)) {
            String token = authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
            authService.logout(token);
        }
        return R.ok();
    }
}
