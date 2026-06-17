package com.mall.user.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String nickname;
    private String avatarUrl;
}
