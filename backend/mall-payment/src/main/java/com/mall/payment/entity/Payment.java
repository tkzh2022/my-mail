package com.mall.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String paymentNo;
    private String tradeNo;
    private String paymentMethod;
    private BigDecimal amount;
    private Integer status;
    private String callbackContent;
    private LocalDateTime paidAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
