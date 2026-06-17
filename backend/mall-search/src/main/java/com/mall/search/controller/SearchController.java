package com.mall.search.controller;

import com.mall.common.result.PageResult;
import com.mall.common.result.R;
import com.mall.search.document.ProductDocument;
import com.mall.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/products")
    public R<PageResult<ProductDocument>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax,
            @RequestParam(defaultValue = "relevance") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        size = Math.max(10, Math.min(100, size));
        return R.ok(searchService.searchProducts(keyword, categoryId, priceMin, priceMax, sort, page, size));
    }

    @GetMapping("/suggest")
    public R<List<String>> suggest(@RequestParam String prefix) {
        if (prefix.length() < 2) return R.ok(List.of());
        return R.ok(searchService.suggest(prefix));
    }
}
