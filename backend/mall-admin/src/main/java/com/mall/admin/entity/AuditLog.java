package com.mall.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** "product" or "merchant" */
    private String targetType;

    private Long targetId;

    private Long adminId;

    /** "approve" or "reject" */
    private String action;

    private String reason;

    private LocalDateTime createdAt;
}
