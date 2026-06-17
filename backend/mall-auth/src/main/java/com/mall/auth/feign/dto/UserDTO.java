package com.mall.auth.feign.dto;

import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String passwordHash;
    private String role;
    private Integer status;
}
