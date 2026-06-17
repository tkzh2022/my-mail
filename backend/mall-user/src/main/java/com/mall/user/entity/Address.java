package com.mall.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("address")
public class Address {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String receiverName;

    private String receiverPhone;

    private String province;

    private String city;

    private String district;

    private String detailAddress;

    private Integer isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
