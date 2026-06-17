package com.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.product.entity.Product;
import com.mall.product.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductMapper productMapper;
    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private ProductService productService;

    @Test
    void listProducts_defaultSort_returnsPageResult() {
        Page<Product> page = new Page<>(1, 10);
        page.setRecords(List.of(createProduct(1L)));
        page.setTotal(1);
        when(productMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<Product> result = productService.listProducts(1, 10, null, null);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    void listProducts_withCategoryFilter() {
        Page<Product> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(productMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<Product> result = productService.listProducts(1, 10, 5L, "price_asc");
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getProductDetail_found_returnsProduct() {
        Product product = createProduct(1L);
        product.setStatus(1);
        when(productMapper.selectById(1L)).thenReturn(product);

        Product result = productService.getProductDetail(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getProductDetail_notFound_throwsBizException() {
        when(productMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> productService.getProductDetail(99L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40404);
    }

    @Test
    void getProductDetail_inactiveStatus_throwsBizException() {
        Product product = createProduct(1L);
        product.setStatus(0);
        when(productMapper.selectById(1L)).thenReturn(product);

        assertThatThrownBy(() -> productService.getProductDetail(1L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40404);
    }

    @Test
    void getRelatedProducts_productNotFound_returnsEmpty() {
        when(productMapper.selectById(1L)).thenReturn(null);
        List<Product> result = productService.getRelatedProducts(1L, 5);
        assertThat(result).isEmpty();
    }

    @Test
    void getRelatedProducts_returnsRelated() {
        Product product = createProduct(1L);
        product.setCategoryId(10L);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productMapper.selectList(any())).thenReturn(List.of(createProduct(2L)));

        List<Product> result = productService.getRelatedProducts(1L, 5);
        assertThat(result).hasSize(1);
    }

    @Test
    void deductStock_success() {
        Product product = createProduct(1L);
        product.setStock(10);
        product.setSalesCount(5);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productMapper.updateById(any())).thenReturn(1);

        boolean result = productService.deductStock(1L, 3);

        assertThat(result).isTrue();
        assertThat(product.getStock()).isEqualTo(7);
        assertThat(product.getSalesCount()).isEqualTo(8);
    }

    @Test
    void deductStock_insufficientStock_returnsFalse() {
        Product product = createProduct(1L);
        product.setStock(2);
        when(productMapper.selectById(1L)).thenReturn(product);

        boolean result = productService.deductStock(1L, 5);
        assertThat(result).isFalse();
    }

    @Test
    void deductStock_productNotFound_returnsFalse() {
        when(productMapper.selectById(99L)).thenReturn(null);
        assertThat(productService.deductStock(99L, 1)).isFalse();
    }

    private Product createProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setStock(100);
        product.setSalesCount(10);
        product.setStatus(1);
        product.setCategoryId(1L);
        product.setMerchantId(1L);
        return product;
    }
}
