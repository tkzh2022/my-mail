package com.mall.seckill.service;

import com.mall.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeckillServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;
    @Mock
    private ListOperations<String, String> listOps;

    private SeckillService seckillService;
    private DefaultRedisScript<Long> mockScript;

    @BeforeEach
    void setUp() {
        seckillService = new SeckillService(redisTemplate);
        mockScript = new DefaultRedisScript<>();
        mockScript.setScriptText("return 1");
        mockScript.setResultType(Long.class);
        ReflectionTestUtils.setField(seckillService, "seckillScript", mockScript);
    }

    @Test
    void attemptPurchase_success_returnsRemainingStock() {
        when(redisTemplate.execute(eq(mockScript), anyList(), any()))
                .thenReturn(99L);

        long result = seckillService.attemptPurchase(1L, 1L);
        assertThat(result).isEqualTo(99L);
    }

    @Test
    void attemptPurchase_soldOut_throwsBizException() {
        when(redisTemplate.execute(eq(mockScript), anyList(), any()))
                .thenReturn(-1L);

        assertThatThrownBy(() -> seckillService.attemptPurchase(1L, 1L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40080);
    }

    @Test
    void attemptPurchase_alreadyPurchased_throwsBizException() {
        when(redisTemplate.execute(eq(mockScript), anyList(), any()))
                .thenReturn(-2L);

        assertThatThrownBy(() -> seckillService.attemptPurchase(1L, 1L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40081);
    }

    @Test
    void attemptPurchase_nullResult_throwsBizException() {
        when(redisTemplate.execute(eq(mockScript), anyList(), any()))
                .thenReturn(null);

        assertThatThrownBy(() -> seckillService.attemptPurchase(1L, 1L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(50001);
    }

    @Test
    void warmUpStock_setsRedisKey() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        seckillService.warmUpStock(1L, 100);

        verify(valueOps).set("seckill:stock:1", "100");
    }

    @Test
    void getQueuePosition_found_returnsPosition() {
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.indexOf("seckill:queue:1", "1")).thenReturn(2L);

        int position = seckillService.getQueuePosition(1L, 1L);
        assertThat(position).isEqualTo(3);
    }

    @Test
    void getQueuePosition_notFound_returnsNegativeOne() {
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.indexOf("seckill:queue:1", "1")).thenReturn(null);

        int position = seckillService.getQueuePosition(1L, 1L);
        assertThat(position).isEqualTo(-1);
    }
}
