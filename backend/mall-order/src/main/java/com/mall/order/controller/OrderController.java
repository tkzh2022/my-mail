package com.mall.order.controller;

import com.mall.common.result.PageResult;
import com.mall.common.result.R;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public R<Order> createOrder(@RequestHeader("X-User-Id") Long userId,
                                @Valid @RequestBody CreateOrderRequest request) {
        return R.ok(orderService.createOrder(userId, request));
    }

    @GetMapping
    public R<PageResult<Order>> listOrders(@RequestHeader("X-User-Id") Long userId,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return R.ok(orderService.listUserOrders(userId, status, page, size));
    }

    @GetMapping("/{orderNo}")
    public R<Map<String, Object>> getOrder(@RequestHeader("X-User-Id") Long userId,
                                            @PathVariable String orderNo) {
        Order order = orderService.getByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            return R.fail(40404, "Order not found");
        }
        List<OrderItem> items = orderService.getOrderItems(order.getId());
        return R.ok(Map.of("order", order, "items", items));
    }

    @PutMapping("/{orderNo}/cancel")
    public R<Void> cancelOrder(@RequestHeader("X-User-Id") Long userId, @PathVariable String orderNo) {
        orderService.cancelOrder(userId, orderNo);
        return R.ok();
    }
}
