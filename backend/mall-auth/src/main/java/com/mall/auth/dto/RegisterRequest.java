package com.mall.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{11}$")
    private String phone;

    @NotBlank
    @Size(min = 8, max = 32)
    private String password;

    @NotBlank
    @Size(min = 6, max = 6)
    private String verificationCode;
}
