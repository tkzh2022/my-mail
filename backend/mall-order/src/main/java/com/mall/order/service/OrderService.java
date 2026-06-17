package com.mall.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.common.util.IdGenerator;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.mapper.OrderItemMapper;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.mq.OrderEventPublisher;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderEventPublisher eventPublisher;
    private final StringRedisTemplate redisTemplate;

    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public Order createOrder(Long userId, CreateOrderRequest request) {
        String idempotencyKey = "order:idempotent:" + request.getIdempotencyKey();
        Boolean setResult = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", 30, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(setResult)) {
            throw new BizException(40050, "Duplicate order submission");
        }

        Order order = new Order();
        order.setOrderNo(IdGenerator.generateOrderNo());
        order.setUserId(userId);
        order.setStatus(0);
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setRemark(request.getRemark());
        order.setFreightAmount(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.OrderItemDTO item : request.getItems()) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);
        order.setPayAmount(total);
        orderMapper.insert(order);

        for (CreateOrderRequest.OrderItemDTO item : request.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setProductImage(item.getProductImage());
            orderItem.setPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setMerchantId(item.getMerchantId());
            orderItemMapper.insert(orderItem);
        }

        eventPublisher.publishOrderCreated(order);
        return order;
    }

    public PageResult<Order> listUserOrders(Long userId, Integer status, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt);
        if (status != null && status >= 0) {
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        PageResult<Order> pr = new PageResult<>();
        pr.setItems(result.getRecords());
        pr.setTotal(result.getTotal());
        pr.setPage(page);
        pr.setSize(size);
        return pr;
    }

    public Order getByOrderNo(String orderNo) {
        return orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }

    public void markPaid(String orderNo, String paymentMethod) {
        Order order = getByOrderNo(orderNo);
        if (order == null || order.getStatus() != 0) return;
        order.setStatus(1);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.now());
        orderMapper.updateById(order);
        eventPublisher.publishOrderPaid(order);
    }

    public void cancelOrder(Long userId, String orderNo) {
        Order order = getByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId) || order.getStatus() != 0) {
            throw new BizException(40051, "Cannot cancel this order");
        }
        order.setStatus(5);
        orderMapper.updateById(order);
        eventPublisher.publishOrderCancelled(order);
    }
}
