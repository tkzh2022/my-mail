package com.mall.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank
    private String username;
    @Email
    private String email;
    @NotBlank
    private String phone;
    @NotBlank
    private String passwordHash;
}
