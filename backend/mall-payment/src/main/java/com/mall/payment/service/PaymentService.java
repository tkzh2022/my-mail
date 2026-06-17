package com.mall.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.exception.BizException;
import com.mall.common.util.IdGenerator;
import com.mall.payment.entity.Payment;
import com.mall.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;

    public Payment createPayment(String orderNo, Long orderId, BigDecimal amount, String paymentMethod) {
        Payment existing = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderId, orderId));
        if (existing != null) {
            return existing;
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentNo(IdGenerator.generateOrderNo());
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setStatus(0);
        paymentMapper.insert(payment);
        return payment;
    }

    public Payment handleCallback(String tradeNo, String paymentNo, String callbackContent) {
        Payment payment = paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getPaymentNo, paymentNo));
        if (payment == null) {
            throw new BizException(40060, "Payment not found");
        }
        if (payment.getStatus() == 1) {
            return payment;
        }

        payment.setTradeNo(tradeNo);
        payment.setStatus(1);
        payment.setCallbackContent(callbackContent);
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.updateById(payment);
        return payment;
    }

    public Payment getByPaymentNo(String paymentNo) {
        return paymentMapper.selectOne(
                new LambdaQueryWrapper<Payment>().eq(Payment::getPaymentNo, paymentNo));
    }
}
