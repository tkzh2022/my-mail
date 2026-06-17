package com.mall.common.trace;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class TraceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        String traceId = request.getHeader(TraceConfig.REQUEST_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(TraceConfig.TRACE_ID_KEY, traceId);
        if (!StringUtils.hasText(MDC.get(TraceConfig.SPAN_ID_KEY))) {
            MDC.put(TraceConfig.SPAN_ID_KEY, UUID.randomUUID().toString().substring(0, 8));
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        MDC.remove(TraceConfig.TRACE_ID_KEY);
        MDC.remove(TraceConfig.SPAN_ID_KEY);
    }
}
