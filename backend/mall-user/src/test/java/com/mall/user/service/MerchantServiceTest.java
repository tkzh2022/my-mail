package com.mall.user.service;

import com.mall.common.exception.BizException;
import com.mall.user.entity.Merchant;
import com.mall.user.mapper.MerchantMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

    @Mock
    private MerchantMapper merchantMapper;

    @InjectMocks
    private MerchantService merchantService;

    @Test
    void register_success() {
        when(merchantMapper.selectCount(any())).thenReturn(0L);
        when(merchantMapper.insert(any(Merchant.class))).thenReturn(1);

        Merchant result = merchantService.register(1L, "MyShop", "LIC123", "John", "13800138000", "desc");

        assertThat(result.getShopName()).isEqualTo("MyShop");
        assertThat(result.getStatus()).isEqualTo(0);
        verify(merchantMapper).insert(any(Merchant.class));
    }

    @Test
    void register_userAlreadyHasMerchant_throwsBizException() {
        when(merchantMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> merchantService.register(1L, "Shop", "LIC", "n", "p", "d"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40010);
    }

    @Test
    void register_duplicateLicense_throwsBizException() {
        when(merchantMapper.selectCount(any())).thenReturn(0L).thenReturn(1L);

        assertThatThrownBy(() -> merchantService.register(1L, "Shop", "LIC", "n", "p", "d"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40011);
    }

    @Test
    void findByUserId_returnsMerchant() {
        Merchant merchant = new Merchant();
        merchant.setId(1L);
        merchant.setUserId(1L);
        when(merchantMapper.selectOne(any())).thenReturn(merchant);

        Merchant result = merchantService.findByUserId(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    void findByUserId_returnsNull() {
        when(merchantMapper.selectOne(any())).thenReturn(null);
        assertThat(merchantService.findByUserId(99L)).isNull();
    }

    @Test
    void findById_returnsMerchant() {
        Merchant merchant = new Merchant();
        merchant.setId(5L);
        when(merchantMapper.selectById(5L)).thenReturn(merchant);

        assertThat(merchantService.findById(5L).getId()).isEqualTo(5L);
    }
}
