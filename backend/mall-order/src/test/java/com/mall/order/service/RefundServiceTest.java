package com.mall.order.service;

import com.mall.common.exception.BizException;
import com.mall.order.entity.Order;
import com.mall.order.entity.Refund;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.mapper.RefundMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundMapper refundMapper;
    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private RefundService refundService;

    @Test
    void createRefund_success_orderPaid() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(1);
        order.setPayAmount(new BigDecimal("100.00"));
        when(orderMapper.selectById(1L)).thenReturn(order);
        when(refundMapper.insert(any(Refund.class))).thenReturn(1);
        when(orderMapper.updateById(any())).thenReturn(1);

        Refund result = refundService.createRefund(1L, 1L, "Defective", List.of("img1.jpg"));

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(order.getStatus()).isEqualTo(6);
    }

    @Test
    void createRefund_orderNotFound_throwsBizException() {
        when(orderMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> refundService.createRefund(1L, 99L, "reason", List.of()))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40070);
    }

    @Test
    void createRefund_wrongUser_throwsBizException() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(2L);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThatThrownBy(() -> refundService.createRefund(1L, 1L, "reason", List.of()))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40070);
    }

    @Test
    void createRefund_invalidOrderStatus_throwsBizException() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(0);
        when(orderMapper.selectById(1L)).thenReturn(order);

        assertThatThrownBy(() -> refundService.createRefund(1L, 1L, "reason", List.of()))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40071);
    }

    @Test
    void merchantApprove_success() {
        Refund refund = new Refund();
        refund.setRefundNo("RF001");
        refund.setStatus(0);
        when(refundMapper.selectOne(any())).thenReturn(refund);
        when(refundMapper.updateById(any())).thenReturn(1);

        refundService.merchantApprove(1L, "RF001");

        assertThat(refund.getStatus()).isEqualTo(1);
        assertThat(refund.getCompletedAt()).isNotNull();
    }

    @Test
    void merchantApprove_invalidState_throwsBizException() {
        Refund refund = new Refund();
        refund.setStatus(1);
        when(refundMapper.selectOne(any())).thenReturn(refund);

        assertThatThrownBy(() -> refundService.merchantApprove(1L, "RF001"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40072);
    }

    @Test
    void merchantReject_success() {
        Refund refund = new Refund();
        refund.setRefundNo("RF001");
        refund.setStatus(0);
        when(refundMapper.selectOne(any())).thenReturn(refund);
        when(refundMapper.updateById(any())).thenReturn(1);

        refundService.merchantReject(1L, "RF001", "No valid reason");

        assertThat(refund.getStatus()).isEqualTo(2);
        assertThat(refund.getMerchantReply()).isEqualTo("No valid reason");
    }

    @Test
    void adminArbitrate_approve() {
        Refund refund = new Refund();
        refund.setRefundNo("RF001");
        refund.setStatus(3);
        when(refundMapper.selectOne(any())).thenReturn(refund);
        when(refundMapper.updateById(any())).thenReturn(1);

        refundService.adminArbitrate(10L, "RF001", true, "Approved by admin");

        assertThat(refund.getStatus()).isEqualTo(4);
        assertThat(refund.getAdminId()).isEqualTo(10L);
        assertThat(refund.getCompletedAt()).isNotNull();
    }

    @Test
    void adminArbitrate_reject() {
        Refund refund = new Refund();
        refund.setRefundNo("RF001");
        refund.setStatus(3);
        when(refundMapper.selectOne(any())).thenReturn(refund);
        when(refundMapper.updateById(any())).thenReturn(1);

        refundService.adminArbitrate(10L, "RF001", false, "Rejected");

        assertThat(refund.getStatus()).isEqualTo(5);
    }

    @Test
    void merchantApprove_refundNotFound_throwsBizException() {
        when(refundMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> refundService.merchantApprove(1L, "RF_NONE"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40073);
    }
}
