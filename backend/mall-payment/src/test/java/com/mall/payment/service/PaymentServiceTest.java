package com.mall.payment.service;

import com.mall.common.exception.BizException;
import com.mall.payment.entity.Payment;
import com.mall.payment.mapper.PaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_newPayment_success() {
        when(paymentMapper.selectOne(any())).thenReturn(null);
        when(paymentMapper.insert(any(Payment.class))).thenReturn(1);

        Payment result = paymentService.createPayment("ORD001", 1L, new BigDecimal("99.99"), "alipay");

        assertThat(result.getOrderId()).isEqualTo(1L);
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
        assertThat(result.getPaymentMethod()).isEqualTo("alipay");
        assertThat(result.getStatus()).isEqualTo(0);
        verify(paymentMapper).insert(any());
    }

    @Test
    void createPayment_existingPayment_returnsExisting() {
        Payment existing = new Payment();
        existing.setId(1L);
        existing.setOrderId(1L);
        when(paymentMapper.selectOne(any())).thenReturn(existing);

        Payment result = paymentService.createPayment("ORD001", 1L, new BigDecimal("99.99"), "alipay");

        assertThat(result.getId()).isEqualTo(1L);
        verify(paymentMapper, never()).insert(any());
    }

    @Test
    void handleCallback_success() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPaymentNo("PAY001");
        payment.setStatus(0);
        when(paymentMapper.selectOne(any())).thenReturn(payment);
        when(paymentMapper.updateById(any())).thenReturn(1);

        Payment result = paymentService.handleCallback("TRADE001", "PAY001", "{\"status\":\"success\"}");

        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getTradeNo()).isEqualTo("TRADE001");
        assertThat(result.getPaidAt()).isNotNull();
    }

    @Test
    void handleCallback_paymentNotFound_throwsBizException() {
        when(paymentMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> paymentService.handleCallback("TRADE", "PAY_NONE", "{}"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40060);
    }

    @Test
    void handleCallback_alreadyPaid_returnsIdempotent() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(1);
        when(paymentMapper.selectOne(any())).thenReturn(payment);

        Payment result = paymentService.handleCallback("TRADE", "PAY001", "{}");

        assertThat(result.getStatus()).isEqualTo(1);
        verify(paymentMapper, never()).updateById(any());
    }

    @Test
    void getByPaymentNo_returnsPayment() {
        Payment payment = new Payment();
        payment.setPaymentNo("PAY001");
        when(paymentMapper.selectOne(any())).thenReturn(payment);

        Payment result = paymentService.getByPaymentNo("PAY001");
        assertThat(result.getPaymentNo()).isEqualTo("PAY001");
    }

    @Test
    void getByPaymentNo_notFound_returnsNull() {
        when(paymentMapper.selectOne(any())).thenReturn(null);
        assertThat(paymentService.getByPaymentNo("NONE")).isNull();
    }
}
