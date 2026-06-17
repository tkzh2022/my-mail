package com.mall.recommend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ZSetOperations<String, String> zSetOps;

    @InjectMocks
    private RecommendService recommendService;

    @Test
    void getPersonalizedRecommendations_withHistory_returnsProductIds() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        Set<String> history = new LinkedHashSet<>(List.of("10", "20", "30"));
        when(zSetOps.reverseRange("user:history:1", 0, 20)).thenReturn(history);

        List<Long> result = recommendService.getPersonalizedRecommendations(1L, 5);

        assertThat(result).containsExactly(10L, 20L, 30L);
    }

    @Test
    void getPersonalizedRecommendations_noHistory_fallsBackToTrending() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.reverseRange("user:history:1", 0, 20)).thenReturn(Collections.emptySet());
        Set<String> trending = new LinkedHashSet<>(List.of("50", "60"));
        when(zSetOps.reverseRange("product:hot", 0, 4)).thenReturn(trending);

        List<Long> result = recommendService.getPersonalizedRecommendations(1L, 5);

        assertThat(result).containsExactly(50L, 60L);
    }

    @Test
    void getPersonalizedRecommendations_nullHistory_fallsBackToTrending() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.reverseRange("user:history:1", 0, 20)).thenReturn(null);
        when(zSetOps.reverseRange("product:hot", 0, 4)).thenReturn(new LinkedHashSet<>(List.of("100")));

        List<Long> result = recommendService.getPersonalizedRecommendations(1L, 5);

        assertThat(result).containsExactly(100L);
    }

    @Test
    void getTrendingProducts_returnsProducts() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        Set<String> trending = new LinkedHashSet<>(List.of("1", "2", "3"));
        when(zSetOps.reverseRange("product:hot", 0, 9)).thenReturn(trending);

        List<Long> result = recommendService.getTrendingProducts(10);

        assertThat(result).containsExactly(1L, 2L, 3L);
    }

    @Test
    void getTrendingProducts_nullResult_returnsEmpty() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
        when(zSetOps.reverseRange("product:hot", 0, 4)).thenReturn(null);

        List<Long> result = recommendService.getTrendingProducts(5);
        assertThat(result).isEmpty();
    }

    @Test
    void trackView_incrementsScores() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        recommendService.trackView(1L, 10L);

        verify(zSetOps).incrementScore("user:history:1", "10", 1.0);
        verify(zSetOps).incrementScore("product:hot", "10", 1.0);
    }

    @Test
    void trackPurchase_incrementsHigherScores() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);

        recommendService.trackPurchase(1L, 10L);

        verify(zSetOps).incrementScore("user:history:1", "10", 5.0);
        verify(zSetOps).incrementScore("product:hot", "10", 3.0);
    }
}
