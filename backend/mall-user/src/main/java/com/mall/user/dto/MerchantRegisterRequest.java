package com.mall.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MerchantRegisterRequest {
    @NotBlank
    private String shopName;
    @NotBlank
    private String businessLicense;
    @NotBlank
    private String contactName;
    @NotBlank
    private String contactPhone;
    private String description;
}
