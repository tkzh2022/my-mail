package com.mall.product.controller;

import com.mall.common.result.PageResult;
import com.mall.common.result.R;
import com.mall.product.entity.Product;
import com.mall.product.service.MerchantProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/products")
@RequiredArgsConstructor
public class MerchantProductController {

    private final MerchantProductService merchantProductService;

    @PostMapping
    public R<Product> createProduct(@RequestHeader("X-User-Id") Long userId,
                                    @RequestBody Product product) {
        return R.ok(merchantProductService.createProduct(userId, product));
    }

    @PutMapping("/{id}")
    public R<Void> updateProduct(@RequestHeader("X-User-Id") Long userId,
                                  @PathVariable Long id, @RequestBody Product product) {
        merchantProductService.updateProduct(userId, id, product);
        return R.ok();
    }

    @PutMapping("/{id}/offline")
    public R<Void> takeOffline(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        merchantProductService.takeOffline(userId, id);
        return R.ok();
    }

    @GetMapping
    public R<PageResult<Product>> listProducts(@RequestHeader("X-User-Id") Long userId,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return R.ok(merchantProductService.listMerchantProducts(userId, status, page, size));
    }
}
