package com.mall.order.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.order.entity.Order;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.mq.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutJob {

    private final OrderMapper orderMapper;
    private final OrderEventPublisher eventPublisher;

    @Scheduled(fixedRate = 60000)
    public void cancelExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(30);
        List<Order> expired = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, 0)
                        .lt(Order::getCreatedAt, deadline));

        for (Order order : expired) {
            order.setStatus(5);
            orderMapper.updateById(order);
            eventPublisher.publishOrderCancelled(order);
            log.info("Auto-cancelled expired order: {}", order.getOrderNo());
        }
    }
}
