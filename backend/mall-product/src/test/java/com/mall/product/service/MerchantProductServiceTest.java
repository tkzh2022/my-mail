package com.mall.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.product.entity.Product;
import com.mall.product.mapper.ProductMapper;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantProductServiceTest {

    @Mock
    private ProductMapper productMapper;
    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @InjectMocks
    private MerchantProductService merchantProductService;

    @Test
    void createProduct_setsStatusToZero() {
        when(productMapper.insert(any())).thenReturn(1);

        Product product = new Product();
        product.setName("New Product");
        product.setPrice(new BigDecimal("99.99"));

        Product result = merchantProductService.createProduct(1L, product);

        assertThat(result.getMerchantId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.getSalesCount()).isEqualTo(0);
    }

    @Test
    void updateProduct_productNotFound_throwsBizException() {
        when(productMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> merchantProductService.updateProduct(1L, 99L, new Product()))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40040);
    }

    @Test
    void updateProduct_wrongMerchant_throwsBizException() {
        Product existing = createProduct(1L, 2L);
        when(productMapper.selectById(1L)).thenReturn(existing);

        assertThatThrownBy(() -> merchantProductService.updateProduct(1L, 1L, new Product()))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40040);
    }

    @Test
    void updateProduct_priceChangeOver20Percent_triggersReAudit() {
        Product existing = createProduct(1L, 1L);
        existing.setPrice(new BigDecimal("100.00"));
        existing.setStatus(1);
        when(productMapper.selectById(1L)).thenReturn(existing);
        when(productMapper.updateById(any())).thenReturn(1);

        Product updated = new Product();
        updated.setPrice(new BigDecimal("130.00"));

        merchantProductService.updateProduct(1L, 1L, updated);

        verify(productMapper).updateById(argThat(p -> ((Product) p).getStatus() == 0));
    }

    @Test
    void updateProduct_priceChangeUnder20Percent_noReAudit() {
        Product existing = createProduct(1L, 1L);
        existing.setPrice(new BigDecimal("100.00"));
        existing.setStatus(1);
        when(productMapper.selectById(1L)).thenReturn(existing);
        when(productMapper.updateById(any())).thenReturn(1);

        Product updated = new Product();
        updated.setPrice(new BigDecimal("110.00"));

        merchantProductService.updateProduct(1L, 1L, updated);

        verify(rocketMQTemplate).send(eq("product-events"), any(Message.class));
    }

    @Test
    void updateProduct_categoryChange_triggersReAudit() {
        Product existing = createProduct(1L, 1L);
        existing.setCategoryId(5L);
        existing.setStatus(1);
        when(productMapper.selectById(1L)).thenReturn(existing);
        when(productMapper.updateById(any())).thenReturn(1);

        Product updated = new Product();
        updated.setCategoryId(10L);

        merchantProductService.updateProduct(1L, 1L, updated);

        verify(productMapper).updateById(argThat(p -> ((Product) p).getStatus() == 0));
    }

    @Test
    void takeOffline_success() {
        Product product = createProduct(1L, 1L);
        product.setStatus(1);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(productMapper.updateById(any())).thenReturn(1);

        merchantProductService.takeOffline(1L, 1L);

        verify(productMapper).updateById(argThat(p -> ((Product) p).getStatus() == 2));
        verify(rocketMQTemplate).send(eq("product-events"), any(Message.class));
    }

    @Test
    void takeOffline_wrongMerchant_throwsBizException() {
        Product product = createProduct(1L, 2L);
        when(productMapper.selectById(1L)).thenReturn(product);

        assertThatThrownBy(() -> merchantProductService.takeOffline(1L, 1L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40040);
    }

    @Test
    void listMerchantProducts_returnsPageResult() {
        Page<Product> page = new Page<>(1, 10);
        page.setRecords(List.of(createProduct(1L, 1L)));
        page.setTotal(1);
        when(productMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<Product> result = merchantProductService.listMerchantProducts(1L, null, 1, 10);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
    }

    private Product createProduct(Long id, Long merchantId) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product");
        product.setPrice(new BigDecimal("50.00"));
        product.setStock(100);
        product.setSalesCount(0);
        product.setStatus(1);
        product.setCategoryId(1L);
        product.setMerchantId(merchantId);
        return product;
    }
}
