package com.mall.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String shopName;

    private String businessLicense;

    private String contactName;

    private String contactPhone;

    private String description;

    private String logoUrl;

    private Integer status;

    private BigDecimal rating;

    private String rejectReason;

    private LocalDateTime approvedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
