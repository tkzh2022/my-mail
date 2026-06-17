package com.mall.seckill.service;

import com.mall.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillService {

    private final StringRedisTemplate redisTemplate;
    private DefaultRedisScript<Long> seckillScript;

    @PostConstruct
    public void init() {
        seckillScript = new DefaultRedisScript<>();
        seckillScript.setLocation(new ClassPathResource("lua/seckill.lua"));
        seckillScript.setResultType(Long.class);
    }

    public long attemptPurchase(Long productId, Long userId) {
        String stockKey = "seckill:stock:" + productId;
        String purchasedKey = "seckill:purchased:" + productId;

        Long result = redisTemplate.execute(seckillScript,
                List.of(stockKey, purchasedKey), String.valueOf(userId));

        if (result == null) {
            throw new BizException(50001, "Seckill service error");
        }
        if (result == -1) {
            throw new BizException(40080, "Sold out");
        }
        if (result == -2) {
            throw new BizException(40081, "Already purchased");
        }
        return result;
    }

    public void warmUpStock(Long productId, int stock) {
        String stockKey = "seckill:stock:" + productId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
        log.info("Warmed up seckill stock for product {}: {}", productId, stock);
    }

    public int getQueuePosition(Long productId, Long userId) {
        String queueKey = "seckill:queue:" + productId;
        Long rank = redisTemplate.opsForList().indexOf(queueKey, String.valueOf(userId));
        return rank == null ? -1 : rank.intValue() + 1;
    }
}
