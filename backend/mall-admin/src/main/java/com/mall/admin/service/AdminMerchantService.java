package com.mall.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.AuditLog;
import com.mall.admin.entity.Merchant;
import com.mall.admin.mapper.AuditLogMapper;
import com.mall.admin.mapper.MerchantMapper;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMerchantService {

    private final MerchantMapper merchantMapper;
    private final AuditLogMapper auditLogMapper;

    public PageResult<Merchant> listPendingMerchants(int page, int size) {
        Page<Merchant> pageParam = new Page<>(page, size);
        Page<Merchant> result = merchantMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getStatus, 0)
                        .orderByAsc(Merchant::getCreatedAt));

        PageResult<Merchant> pageResult = new PageResult<>();
        pageResult.setItems(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setPage(page);
        pageResult.setSize(size);
        return pageResult;
    }

    @Transactional
    public void approveMerchant(Long adminId, Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BizException(40402, "Merchant not found");
        }
        merchant.setStatus(1);
        merchantMapper.updateById(merchant);

        logAudit(adminId, "merchant", merchantId, "approve", null);
        log.info("Admin {} approved merchant {}", adminId, merchantId);
    }

    @Transactional
    public void rejectMerchant(Long adminId, Long merchantId, String reason) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new BizException(40402, "Merchant not found");
        }
        merchant.setStatus(2);
        merchantMapper.updateById(merchant);

        logAudit(adminId, "merchant", merchantId, "reject", reason);
        log.info("Admin {} rejected merchant {} for: {}", adminId, merchantId, reason);
    }

    private void logAudit(Long adminId, String targetType, Long targetId, String action, String reason) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAdminId(adminId);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setAction(action);
        auditLog.setReason(reason);
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(auditLog);
    }
}
