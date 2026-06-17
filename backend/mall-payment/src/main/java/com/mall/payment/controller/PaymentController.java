package com.mall.payment.controller;

import com.mall.common.result.R;
import com.mall.payment.entity.Payment;
import com.mall.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public R<Map<String, Object>> createPayment(@RequestBody Map<String, Object> body) {
        String orderNo = (String) body.get("orderNo");
        Long orderId = Long.valueOf(body.get("orderId").toString());
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        String method = (String) body.get("paymentMethod");

        Payment payment = paymentService.createPayment(orderNo, orderId, amount, method);

        String payUrl = "https://sandbox.alipay.com/pay?no=" + payment.getPaymentNo();
        if ("wechat_pay".equals(method)) {
            payUrl = "weixin://wxpay/bizpayurl?no=" + payment.getPaymentNo();
        }

        return R.ok(Map.of(
                "paymentNo", payment.getPaymentNo(),
                "payUrl", payUrl,
                "expiresIn", 1800
        ));
    }

    @GetMapping("/{paymentNo}/status")
    public R<Payment> getStatus(@PathVariable String paymentNo) {
        return R.ok(paymentService.getByPaymentNo(paymentNo));
    }

    @PostMapping("/callback/alipay")
    public String alipayCallback(@RequestBody String body) {
        paymentService.handleCallback("alipay-trade-" + System.currentTimeMillis(),
                extractPaymentNo(body), body);
        return "success";
    }

    @PostMapping("/callback/wechat")
    public String wechatCallback(@RequestBody String body) {
        paymentService.handleCallback("wechat-trade-" + System.currentTimeMillis(),
                extractPaymentNo(body), body);
        return "success";
    }

    private String extractPaymentNo(String body) {
        return body.contains("paymentNo") ? body.split("paymentNo=")[1].split("&")[0] : "";
    }
}
