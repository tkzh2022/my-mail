package com.mall.auth.dto;

import lombok.Data;

@Data
public class AuthResponse {

    private Long userId;
    private String username;
    private String role;
    private String token;
    private String refreshToken;
    private long expiresIn;
}
