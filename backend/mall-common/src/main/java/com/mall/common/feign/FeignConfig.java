package com.mall.common.feign;

import com.mall.common.trace.TraceConfig;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignConfig {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            copyHeader(request, USER_ID_HEADER, template::header);
            copyHeader(request, USER_ROLE_HEADER, template::header);
            copyHeader(request, TraceConfig.REQUEST_ID_HEADER, template::header);
        };
    }

    @Bean
    public Request.Options feignRequestOptions() {
        return new Request.Options(5000, TimeUnit.MILLISECONDS, 10000, TimeUnit.MILLISECONDS, true);
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3);
    }

    @Bean
    public FeignErrorDecoder feignErrorDecoder(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
        return new FeignErrorDecoder(objectMapper);
    }

    private static void copyHeader(HttpServletRequest request, String headerName, HeaderConsumer consumer) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            consumer.accept(headerName, value);
        }
    }

    @FunctionalInterface
    private interface HeaderConsumer {
        void accept(String name, String value);
    }
}
