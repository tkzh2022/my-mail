package com.mall.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.AuditLog;
import com.mall.admin.entity.Product;
import com.mall.admin.mapper.AuditLogMapper;
import com.mall.admin.mapper.ProductMapper;
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
public class AdminProductService {

    private final ProductMapper productMapper;
    private final AuditLogMapper auditLogMapper;

    public PageResult<Product> listPendingAudit(int page, int size) {
        Page<Product> pageParam = new Page<>(page, size);
        Page<Product> result = productMapper.selectPage(pageParam,
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 0)
                        .orderByAsc(Product::getCreatedAt));

        PageResult<Product> pageResult = new PageResult<>();
        pageResult.setItems(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setPage(page);
        pageResult.setSize(size);
        return pageResult;
    }

    @Transactional
    public void approveProduct(Long adminId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(40401, "Product not found");
        }
        product.setStatus(1);
        productMapper.updateById(product);

        logAudit(adminId, "product", productId, "approve", null);
        log.info("Admin {} approved product {}", adminId, productId);
    }

    @Transactional
    public void rejectProduct(Long adminId, Long productId, String reason) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(40401, "Product not found");
        }
        product.setStatus(3);
        productMapper.updateById(product);

        logAudit(adminId, "product", productId, "reject", reason);
        log.info("Admin {} rejected product {} for: {}", adminId, productId, reason);
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
