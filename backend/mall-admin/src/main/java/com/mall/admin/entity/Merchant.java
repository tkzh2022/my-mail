package com.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
    private Integer status;
    private LocalDateTime createdAt;
}
