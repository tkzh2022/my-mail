package com.mall.payment.service;

import com.mall.common.exception.BizException;
import com.mall.payment.entity.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void createPayment_andQuery_fullFlow() {
        Payment payment = paymentService.createPayment("ORD001", 1L, new BigDecimal("99.99"), "alipay");

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getPaymentNo()).isNotBlank();
        assertThat(payment.getStatus()).isEqualTo(0);

        Payment found = paymentService.getByPaymentNo(payment.getPaymentNo());
        assertThat(found).isNotNull();
        assertThat(found.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    void createPayment_duplicateOrder_returnsExisting() {
        Payment first = paymentService.createPayment("ORD002", 2L, new BigDecimal("50.00"), "wechat");
        Payment second = paymentService.createPayment("ORD002", 2L, new BigDecimal("50.00"), "wechat");

        assertThat(second.getId()).isEqualTo(first.getId());
    }

    @Test
    void handleCallback_completesPayment() {
        Payment payment = paymentService.createPayment("ORD003", 3L, new BigDecimal("200.00"), "alipay");

        Payment completed = paymentService.handleCallback("TRADE001", payment.getPaymentNo(), "{\"success\":true}");

        assertThat(completed.getStatus()).isEqualTo(1);
        assertThat(completed.getTradeNo()).isEqualTo("TRADE001");
        assertThat(completed.getPaidAt()).isNotNull();
    }

    @Test
    void handleCallback_idempotent_alreadyPaid() {
        Payment payment = paymentService.createPayment("ORD004", 4L, new BigDecimal("100.00"), "alipay");
        paymentService.handleCallback("TRADE002", payment.getPaymentNo(), "{}");

        Payment second = paymentService.handleCallback("TRADE003", payment.getPaymentNo(), "{}");
        assertThat(second.getTradeNo()).isEqualTo("TRADE002");
    }

    @Test
    void handleCallback_paymentNotFound_throwsException() {
        assertThatThrownBy(() -> paymentService.handleCallback("TRADE", "PAY_NONE", "{}"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40060);
    }
}
