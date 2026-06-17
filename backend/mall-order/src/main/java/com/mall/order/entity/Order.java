package com.mall.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private BigDecimal freightAmount;
    private Integer status;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private LocalDateTime shippingTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime completeTime;
    private String trackingCompany;
    private String trackingNumber;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    @Version
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
