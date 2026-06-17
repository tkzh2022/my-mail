package com.mall.notification.listener;

import com.mall.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "order-events", consumerGroup = "notification-order-consumer")
public class OrderNotificationListener implements RocketMQListener<Map<String, Object>> {

    private final NotificationService notificationService;

    @Override
    public void onMessage(Map<String, Object> message) {
        try {
            String event = (String) message.get("event");
            Long userId = ((Number) message.get("userId")).longValue();
            String orderNo = (String) message.get("orderNo");

            switch (event) {
                case "order_created" -> notificationService.send(
                        userId,
                        "订单创建成功",
                        "您的订单 " + orderNo + " 已创建，请在30分钟内完成支付",
                        "order",
                        "in_app");
                case "order_paid" -> notificationService.send(
                        userId,
                        "支付成功",
                        "订单 " + orderNo + " 支付成功，商家将尽快发货",
                        "payment",
                        "in_app");
                case "order_cancelled" -> notificationService.send(
                        userId,
                        "订单已取消",
                        "订单 " + orderNo + " 已取消",
                        "order",
                        "in_app");
                default -> log.warn("Unknown order event: {}", event);
            }
        } catch (Exception e) {
            log.error("Failed to process order notification message: {}", message, e);
        }
    }
}
