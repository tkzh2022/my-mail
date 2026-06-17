package com.mall.order.controller;

import com.mall.common.result.R;
import com.mall.order.entity.Refund;
import com.mall.order.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping("/api/orders/{orderId}/refund")
    public R<Refund> createRefund(@RequestHeader("X-User-Id") Long userId,
                                   @PathVariable Long orderId,
                                   @RequestBody Map<String, Object> body) {
        String reason = (String) body.get("reason");
        List<String> images = (List<String>) body.get("evidenceImages");
        return R.ok(refundService.createRefund(userId, orderId, reason, images));
    }

    @PutMapping("/api/merchant/refunds/{refundNo}/approve")
    public R<Void> merchantApprove(@RequestHeader("X-User-Id") Long userId, @PathVariable String refundNo) {
        refundService.merchantApprove(userId, refundNo);
        return R.ok();
    }

    @PutMapping("/api/merchant/refunds/{refundNo}/reject")
    public R<Void> merchantReject(@RequestHeader("X-User-Id") Long userId, @PathVariable String refundNo,
                                   @RequestBody Map<String, String> body) {
        refundService.merchantReject(userId, refundNo, body.get("reply"));
        return R.ok();
    }
}
