package com.mall.order.mq;

import com.mall.order.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RocketMQTemplate rocketMQTemplate;

    public void publishOrderCreated(Order order) {
        send("order-events", "ORDER_CREATED", order);
    }

    public void publishOrderPaid(Order order) {
        send("order-events", "ORDER_PAID", order);
    }

    public void publishOrderCancelled(Order order) {
        send("order-events", "ORDER_CANCELLED", order);
    }

    private void send(String topic, String eventType, Order order) {
        Map<String, Object> payload = Map.of(
                "eventType", eventType,
                "orderNo", order.getOrderNo(),
                "userId", order.getUserId(),
                "status", order.getStatus()
        );
        rocketMQTemplate.send(topic, MessageBuilder.withPayload(payload).build());
        log.info("Published {} for order {}", eventType, order.getOrderNo());
    }
}
