package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.product.entity.Product;
import com.mall.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;

    public PageResult<Product> listProducts(int page, int size, Long categoryId, String sort) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getPrice);
        } else if ("sales".equals(sort)) {
            wrapper.orderByDesc(Product::getSalesCount);
        } else {
            wrapper.orderByDesc(Product::getCreatedAt);
        }

        Page<Product> result = productMapper.selectPage(pageParam, wrapper);

        PageResult<Product> pageResult = new PageResult<>();
        pageResult.setItems(result.getRecords());
        pageResult.setTotal(result.getTotal());
        pageResult.setPage(page);
        pageResult.setSize(size);
        return pageResult;
    }

    public Product getProductDetail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            throw new BizException(40404, "Product not found");
        }
        return product;
    }

    public List<Product> getRelatedProducts(Long productId, int limit) {
        Product product = productMapper.selectById(productId);
        if (product == null) return List.of();

        return productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, product.getCategoryId())
                        .eq(Product::getStatus, 1)
                        .ne(Product::getId, productId)
                        .orderByDesc(Product::getSalesCount)
                        .last("LIMIT " + limit));
    }

    public boolean deductStock(Long productId, int quantity) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStock() < quantity) {
            return false;
        }
        product.setStock(product.getStock() - quantity);
        product.setSalesCount(product.getSalesCount() + quantity);
        return productMapper.updateById(product) > 0;
    }
}
