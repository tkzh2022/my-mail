package com.mall.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "refund", autoResultMap = true)
public class Refund {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String refundNo;
    private Long orderId;
    private Long userId;
    private Long merchantId;
    private BigDecimal amount;
    private String reason;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> evidenceImages;
    private Integer status;
    private String merchantReply;
    private String adminDecision;
    private Long adminId;
    private LocalDateTime completedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
