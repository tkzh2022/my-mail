package com.mall.product.controller;

import com.mall.common.result.PageResult;
import com.mall.common.result.R;
import com.mall.product.entity.Category;
import com.mall.product.entity.Product;
import com.mall.product.service.CategoryService;
import com.mall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public R<PageResult<Product>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "newest") String sort) {
        size = Math.max(10, Math.min(100, size));
        return R.ok(productService.listProducts(page, size, categoryId, sort));
    }

    @GetMapping("/{id}")
    public R<Product> getProduct(@PathVariable Long id) {
        return R.ok(productService.getProductDetail(id));
    }

    @GetMapping("/{id}/related")
    public R<List<Product>> getRelatedProducts(@PathVariable Long id) {
        return R.ok(productService.getRelatedProducts(id, 6));
    }

    @GetMapping("/categories")
    public R<List<Category>> getCategories() {
        return R.ok(categoryService.getCategoryTree());
    }
}
