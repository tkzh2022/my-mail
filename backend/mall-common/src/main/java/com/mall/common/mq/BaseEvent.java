package com.mall.common.mq;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public abstract class BaseEvent<T> {

    private final String eventId;
    private final String eventType;
    private final LocalDateTime timestamp;

    protected BaseEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public abstract T getPayload();
}
