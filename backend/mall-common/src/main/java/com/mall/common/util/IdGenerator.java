package com.mall.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private static volatile String lastTimestamp = "";

    private IdGenerator() {
    }

    public static String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(ORDER_NO_FORMATTER);
        if (!timestamp.equals(lastTimestamp)) {
            synchronized (IdGenerator.class) {
                if (!timestamp.equals(lastTimestamp)) {
                    SEQUENCE.set(0);
                    lastTimestamp = timestamp;
                }
            }
        }
        return timestamp + String.format("%06d", SEQUENCE.incrementAndGet() % 1_000_000);
    }
}
