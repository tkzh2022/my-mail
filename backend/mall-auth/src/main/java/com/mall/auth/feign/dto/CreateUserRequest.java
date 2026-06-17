package com.mall.auth.feign.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequest {

    private String username;
    private String email;
    private String phone;
    private String passwordHash;
    private String role;
}
