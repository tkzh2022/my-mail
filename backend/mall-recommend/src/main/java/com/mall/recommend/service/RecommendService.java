package com.mall.recommend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final StringRedisTemplate redisTemplate;

    private static final String HOT_PRODUCTS_KEY = "product:hot";
    private static final String USER_HISTORY_KEY = "user:history:";

    public List<Long> getPersonalizedRecommendations(Long userId, int limit) {
        String historyKey = USER_HISTORY_KEY + userId;
        Set<String> history = redisTemplate.opsForZSet().reverseRange(historyKey, 0, 20);
        if (history == null || history.isEmpty()) {
            return getTrendingProducts(limit);
        }
        return history.stream()
                .limit(limit)
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    public List<Long> getTrendingProducts(int limit) {
        Set<String> trending = redisTemplate.opsForZSet().reverseRange(HOT_PRODUCTS_KEY, 0, limit - 1);
        if (trending == null) return Collections.emptyList();
        return trending.stream().map(Long::valueOf).collect(Collectors.toList());
    }

    public void trackView(Long userId, Long productId) {
        String historyKey = USER_HISTORY_KEY + userId;
        redisTemplate.opsForZSet().incrementScore(historyKey, String.valueOf(productId), 1.0);
        redisTemplate.opsForZSet().incrementScore(HOT_PRODUCTS_KEY, String.valueOf(productId), 1.0);
    }

    public void trackPurchase(Long userId, Long productId) {
        String historyKey = USER_HISTORY_KEY + userId;
        redisTemplate.opsForZSet().incrementScore(historyKey, String.valueOf(productId), 5.0);
        redisTemplate.opsForZSet().incrementScore(HOT_PRODUCTS_KEY, String.valueOf(productId), 3.0);
    }
}
