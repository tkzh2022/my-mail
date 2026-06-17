package com.mall.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final ObjectMapper objectMapper;

    @Value("${mall.jwt.secret}")
    private String jwtSecret;

    public AuthGlobalFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (shouldSkip(exchange)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Missing or invalid authorization token");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            return unauthorized(exchange, "Missing or invalid authorization token");
        }

        try {
            Claims claims = parseClaims(token);
            String userId = resolveClaim(claims, "userId", "sub");
            String role = resolveClaim(claims, "role");

            if (userId == null || userId.isBlank()) {
                return unauthorized(exchange, "Invalid authorization token");
            }

            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(USER_ID_HEADER, userId)
                    .header(USER_ROLE_HEADER, role != null ? role : "")
                    .build();

            return chain.filter(exchange.mutate().request(request).build());
        } catch (JwtException | IllegalArgumentException ex) {
            return unauthorized(exchange, "Invalid or expired authorization token");
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean shouldSkip(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if (path.startsWith("/api/auth")) {
            return true;
        }
        if (path.startsWith("/api/search")) {
            return true;
        }
        if (path.equals("/api/products/categories") || path.startsWith("/api/products/categories/")) {
            return true;
        }
        if (HttpMethod.GET.equals(method) && "/api/products".equals(path)) {
            return true;
        }
        return false;
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String resolveClaim(Claims claims, String... names) {
        for (String name : names) {
            Object value = claims.get(name);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", HttpStatus.UNAUTHORIZED.value());
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());
        body.put("requestId", Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(RequestIdFilter.REQUEST_ID_HEADER)
        ).orElse(""));

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (JsonProcessingException ex) {
            bytes = ("{\"code\":401,\"message\":\"Unauthorized\"}").getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
