package com.mall.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.AuditLog;
import com.mall.admin.entity.Product;
import com.mall.admin.mapper.AuditLogMapper;
import com.mall.admin.mapper.ProductMapper;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProductServiceTest {

    @Mock
    private ProductMapper productMapper;
    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AdminProductService adminProductService;

    @Test
    void listPendingAudit_returnsPendingProducts() {
        Page<Product> page = new Page<>(1, 10);
        Product product = new Product();
        product.setId(1L);
        product.setStatus(0);
        page.setRecords(List.of(product));
        page.setTotal(1);
        when(productMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<Product> result = adminProductService.listPendingAudit(1, 10);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void approveProduct_success() {
        Product product = new Product();
        product.setId(1L);
        product.setStatus(0);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productMapper.updateById(any())).thenReturn(1);
        when(auditLogMapper.insert(any(AuditLog.class))).thenReturn(1);

        adminProductService.approveProduct(10L, 1L);

        assertThat(product.getStatus()).isEqualTo(1);
        verify(auditLogMapper).insert(argThat(log -> {
            AuditLog l = (AuditLog) log;
            return l.getAdminId().equals(10L) && "approve".equals(l.getAction());
        }));
    }

    @Test
    void approveProduct_notFound_throwsBizException() {
        when(productMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> adminProductService.approveProduct(10L, 99L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40401);
    }

    @Test
    void rejectProduct_success() {
        Product product = new Product();
        product.setId(1L);
        product.setStatus(0);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productMapper.updateById(any())).thenReturn(1);
        when(auditLogMapper.insert(any(AuditLog.class))).thenReturn(1);

        adminProductService.rejectProduct(10L, 1L, "Bad content");

        assertThat(product.getStatus()).isEqualTo(3);
        verify(auditLogMapper).insert(argThat(log -> {
            AuditLog l = (AuditLog) log;
            return "reject".equals(l.getAction()) && "Bad content".equals(l.getReason());
        }));
    }

    @Test
    void rejectProduct_notFound_throwsBizException() {
        when(productMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> adminProductService.rejectProduct(10L, 99L, "reason"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40401);
    }
}
