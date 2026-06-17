package com.mall.recommend.controller;

import com.mall.common.result.R;
import com.mall.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping("/personalized")
    public R<List<Long>> getPersonalized(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                                          @RequestParam(defaultValue = "10") int limit) {
        if (userId == null) {
            return R.ok(recommendService.getTrendingProducts(limit));
        }
        return R.ok(recommendService.getPersonalizedRecommendations(userId, limit));
    }

    @GetMapping("/trending")
    public R<List<Long>> getTrending(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(recommendService.getTrendingProducts(limit));
    }

    @PostMapping("/track/view")
    public R<Void> trackView(@RequestHeader("X-User-Id") Long userId, @RequestParam Long productId) {
        recommendService.trackView(userId, productId);
        return R.ok();
    }
}
