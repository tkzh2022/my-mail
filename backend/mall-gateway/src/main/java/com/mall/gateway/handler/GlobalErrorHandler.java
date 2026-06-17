package com.mall.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.gateway.filter.RequestIdFilter;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Order(-1)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GlobalErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = resolveStatus(ex);
        String message = resolveMessage(status);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", status.value());
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());
        body.put("requestId", Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(RequestIdFilter.REQUEST_ID_HEADER)
        ).orElse(""));

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            bytes = fallbackBody(status.value(), message);
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof NotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof ResponseStatusException responseStatusException) {
            return HttpStatus.valueOf(responseStatusException.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Resource not found";
            case SERVICE_UNAVAILABLE -> "Service unavailable";
            default -> "Internal server error";
        };
    }

    private byte[] fallbackBody(int code, String message) {
        String json = String.format(
                "{\"code\":%d,\"message\":\"%s\",\"timestamp\":\"%s\",\"requestId\":\"\"}",
                code,
                message,
                Instant.now()
        );
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
