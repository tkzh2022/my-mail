package com.mall.admin.controller;

import com.mall.admin.entity.Product;
import com.mall.admin.service.AdminProductService;
import com.mall.common.result.PageResult;
import com.mall.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping("/pending")
    public R<PageResult<Product>> listPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(adminProductService.listPendingAudit(page, size));
    }

    @PostMapping("/{id}/approve")
    public R<Void> approve(
            @RequestHeader("X-User-Id") Long adminId,
            @PathVariable Long id) {
        adminProductService.approveProduct(adminId, id);
        return R.ok();
    }

    @PostMapping("/{id}/reject")
    public R<Void> reject(
            @RequestHeader("X-User-Id") Long adminId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        adminProductService.rejectProduct(adminId, id, body.get("reason"));
        return R.ok();
    }
}
