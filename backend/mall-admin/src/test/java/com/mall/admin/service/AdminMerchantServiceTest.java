package com.mall.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.entity.AuditLog;
import com.mall.admin.entity.Merchant;
import com.mall.admin.mapper.AuditLogMapper;
import com.mall.admin.mapper.MerchantMapper;
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
class AdminMerchantServiceTest {

    @Mock
    private MerchantMapper merchantMapper;
    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AdminMerchantService adminMerchantService;

    @Test
    void listPendingMerchants_returnsPending() {
        Page<Merchant> page = new Page<>(1, 10);
        Merchant merchant = new Merchant();
        merchant.setId(1L);
        merchant.setStatus(0);
        page.setRecords(List.of(merchant));
        page.setTotal(1);
        when(merchantMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<Merchant> result = adminMerchantService.listPendingMerchants(1, 10);

        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void approveMerchant_success() {
        Merchant merchant = new Merchant();
        merchant.setId(1L);
        merchant.setStatus(0);
        when(merchantMapper.selectById(1L)).thenReturn(merchant);
        when(merchantMapper.updateById(any())).thenReturn(1);
        when(auditLogMapper.insert(any(AuditLog.class))).thenReturn(1);

        adminMerchantService.approveMerchant(10L, 1L);

        assertThat(merchant.getStatus()).isEqualTo(1);
        verify(auditLogMapper).insert(any(AuditLog.class));
    }

    @Test
    void approveMerchant_notFound_throwsBizException() {
        when(merchantMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> adminMerchantService.approveMerchant(10L, 99L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40402);
    }

    @Test
    void rejectMerchant_success() {
        Merchant merchant = new Merchant();
        merchant.setId(1L);
        merchant.setStatus(0);
        when(merchantMapper.selectById(1L)).thenReturn(merchant);
        when(merchantMapper.updateById(any())).thenReturn(1);
        when(auditLogMapper.insert(any(AuditLog.class))).thenReturn(1);

        adminMerchantService.rejectMerchant(10L, 1L, "Incomplete docs");

        assertThat(merchant.getStatus()).isEqualTo(2);
    }

    @Test
    void rejectMerchant_notFound_throwsBizException() {
        when(merchantMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> adminMerchantService.rejectMerchant(10L, 99L, "reason"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40402);
    }
}
