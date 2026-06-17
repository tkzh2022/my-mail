package com.mall.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "product", autoResultMap = true)
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long merchantId;

    private Long categoryId;

    private String name;

    private String subtitle;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private Integer stock;

    private Integer salesCount;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    private Integer status;

    private String rejectReason;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
