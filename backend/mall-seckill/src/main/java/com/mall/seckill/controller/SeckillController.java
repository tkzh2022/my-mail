package com.mall.seckill.controller;

import com.mall.common.result.R;
import com.mall.seckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @PostMapping("/purchase/{productId}")
    public R<Map<String, Object>> purchase(@RequestHeader("X-User-Id") Long userId,
                                            @PathVariable Long productId) {
        long result = seckillService.attemptPurchase(productId, userId);
        return R.ok(Map.of("success", true, "message", "Purchase successful"));
    }

    @GetMapping("/queue/{productId}")
    public R<Map<String, Object>> getQueueStatus(@RequestHeader("X-User-Id") Long userId,
                                                   @PathVariable Long productId) {
        int position = seckillService.getQueuePosition(productId, userId);
        return R.ok(Map.of("position", position, "inQueue", position > 0));
    }
}
