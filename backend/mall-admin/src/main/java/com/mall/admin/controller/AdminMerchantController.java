package com.mall.admin.controller;

import com.mall.admin.entity.Merchant;
import com.mall.admin.service.AdminMerchantService;
import com.mall.common.result.PageResult;
import com.mall.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/merchants")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    @GetMapping("/pending")
    public R<PageResult<Merchant>> listPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(adminMerchantService.listPendingMerchants(page, size));
    }

    @PostMapping("/{id}/approve")
    public R<Void> approve(
            @RequestHeader("X-User-Id") Long adminId,
            @PathVariable Long id) {
        adminMerchantService.approveMerchant(adminId, id);
        return R.ok();
    }

    @PostMapping("/{id}/reject")
    public R<Void> reject(
            @RequestHeader("X-User-Id") Long adminId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        adminMerchantService.rejectMerchant(adminId, id, body.get("reason"));
        return R.ok();
    }
}
