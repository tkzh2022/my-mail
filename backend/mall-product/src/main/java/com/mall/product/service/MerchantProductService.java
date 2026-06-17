package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.product.entity.Product;
import com.mall.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantProductService {

    private final ProductMapper productMapper;
    private final RocketMQTemplate rocketMQTemplate;

    public Product createProduct(Long merchantId, Product product) {
        product.setMerchantId(merchantId);
        product.setStatus(0);
        product.setSalesCount(0);
        productMapper.insert(product);
        return product;
    }

    public void updateProduct(Long merchantId, Long productId, Product updated) {
        Product existing = productMapper.selectById(productId);
        if (existing == null || !existing.getMerchantId().equals(merchantId)) {
            throw new BizException(40040, "Product not found");
        }

        boolean needsReAudit = false;
        if (updated.getPrice() != null && existing.getPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal changeRatio = updated.getPrice().subtract(existing.getPrice())
                    .abs().divide(existing.getPrice(), 2, BigDecimal.ROUND_HALF_UP);
            if (changeRatio.compareTo(new BigDecimal("0.20")) > 0) {
                needsReAudit = true;
            }
        }
        if (updated.getCategoryId() != null && !updated.getCategoryId().equals(existing.getCategoryId())) {
            needsReAudit = true;
        }

        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getSubtitle() != null) existing.setSubtitle(updated.getSubtitle());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getPrice() != null) existing.setPrice(updated.getPrice());
        if (updated.getOriginalPrice() != null) existing.setOriginalPrice(updated.getOriginalPrice());
        if (updated.getStock() != null) existing.setStock(updated.getStock());
        if (updated.getImages() != null) existing.setImages(updated.getImages());
        if (updated.getCategoryId() != null) existing.setCategoryId(updated.getCategoryId());

        if (needsReAudit) {
            existing.setStatus(0);
        }
        productMapper.updateById(existing);

        if (existing.getStatus() == 1) {
            publishProductEvent(existing);
        }
    }

    public void takeOffline(Long merchantId, Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !product.getMerchantId().equals(merchantId)) {
            throw new BizException(40040, "Product not found");
        }
        product.setStatus(2);
        productMapper.updateById(product);
        publishProductEvent(product);
    }

    public PageResult<Product> listMerchantProducts(Long merchantId, Integer status, int page, int size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getMerchantId, merchantId)
                .orderByDesc(Product::getCreatedAt);
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        Page<Product> result = productMapper.selectPage(pageParam, wrapper);
        PageResult<Product> pr = new PageResult<>();
        pr.setItems(result.getRecords());
        pr.setTotal(result.getTotal());
        pr.setPage(page);
        pr.setSize(size);
        return pr;
    }

    private void publishProductEvent(Product product) {
        Map<String, Object> event = Map.of(
                "id", product.getId(),
                "name", product.getName(),
                "status", product.getStatus(),
                "merchantId", product.getMerchantId()
        );
        rocketMQTemplate.send("product-events", MessageBuilder.withPayload(event).build());
    }
}
