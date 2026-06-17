package com.mall.common.trace;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class TraceConfig implements WebMvcConfigurer {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String SPAN_ID_KEY = "spanId";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final TraceInterceptor traceInterceptor;

    public TraceConfig(TraceInterceptor traceInterceptor) {
        this.traceInterceptor = traceInterceptor;
    }

    @PostConstruct
    public void configureMdcLoggingPattern() {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) {
            return;
        }
        ch.qos.logback.classic.Logger rootLogger = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        ConsoleAppender<ILoggingEvent> appender = (ConsoleAppender<ILoggingEvent>) rootLogger.getAppender("CONSOLE");
        if (appender == null) {
            return;
        }
        if (appender.getEncoder() instanceof PatternLayoutEncoder encoder) {
            String pattern = encoder.getPattern();
            if (pattern != null && !pattern.contains(TRACE_ID_KEY)) {
                encoder.setPattern(pattern.replace("%msg", "[%X{" + TRACE_ID_KEY + ":-}/%X{" + SPAN_ID_KEY + ":-}] %msg"));
                encoder.start();
            }
        }
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> traceFilterRegistration() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                              HttpServletResponse response,
                                              FilterChain filterChain) throws ServletException, IOException {
                String requestId = request.getHeader(REQUEST_ID_HEADER);
                if (!StringUtils.hasText(requestId)) {
                    requestId = UUID.randomUUID().toString();
                }
                MDC.put(TRACE_ID_KEY, requestId);
                MDC.put(SPAN_ID_KEY, UUID.randomUUID().toString().substring(0, 8));
                response.setHeader(REQUEST_ID_HEADER, requestId);
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    MDC.remove(TRACE_ID_KEY);
                    MDC.remove(SPAN_ID_KEY);
                }
            }
        });
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceInterceptor).addPathPatterns("/**");
    }
}
