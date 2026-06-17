package com.mall.common.mq;

import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    public static final String ORDER_EVENTS = "order-events";
    public static final String REFUND_EVENTS = "refund-events";
    public static final String PRODUCT_EVENTS = "product-events";
    public static final String NOTIFICATION_EVENTS = "notification-events";
}
