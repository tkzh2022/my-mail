package com.mall.cart.controller;

import com.mall.cart.entity.Cart;
import com.mall.cart.service.CartService;
import com.mall.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public R<List<Cart>> listCart(@RequestHeader("X-User-Id") Long userId) {
        return R.ok(cartService.getUserCart(userId));
    }

    @PostMapping("/items")
    public R<Cart> addItem(@RequestHeader("X-User-Id") Long userId, @RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(body.get("productId").toString());
        int quantity = Integer.parseInt(body.getOrDefault("quantity", 1).toString());
        return R.ok(cartService.addItem(userId, productId, quantity));
    }

    @PutMapping("/items/{id}/quantity")
    public R<Void> updateQuantity(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id,
                                   @RequestBody Map<String, Integer> body) {
        cartService.updateQuantity(userId, id, body.get("quantity"));
        return R.ok();
    }

    @DeleteMapping("/items/{id}")
    public R<Void> removeItem(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        cartService.removeItem(userId, id);
        return R.ok();
    }
}
