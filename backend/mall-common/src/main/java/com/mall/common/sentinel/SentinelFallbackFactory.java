package com.mall.common.sentinel;

import com.mall.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

@Slf4j
public abstract class SentinelFallbackFactory<T> implements FallbackFactory<T> {

    @Override
    public T create(Throwable cause) {
        log.error("Feign client fallback triggered: {}", cause.getMessage(), cause);
        return createFallback(fallbackResponse(cause));
    }

    protected R<Void> fallbackResponse(Throwable cause) {
        String message = cause.getMessage() != null ? cause.getMessage() : "unknown error";
        return R.fail(503, "Service unavailable: " + message);
    }

    protected abstract T createFallback(R<Void> errorResponse);
}
