package com.mall.cart.service;

import com.mall.cart.entity.Cart;
import com.mall.cart.mapper.CartMapper;
import com.mall.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    @Test
    void getUserCart_returnsItems() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        when(cartMapper.selectList(any())).thenReturn(List.of(cart));

        List<Cart> result = cartService.getUserCart(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void addItem_newItem_createsCartEntry() {
        when(cartMapper.selectOne(any())).thenReturn(null);
        when(cartMapper.insert(any())).thenReturn(1);

        Cart result = cartService.addItem(1L, 10L, 2);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getProductId()).isEqualTo(10L);
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getSelected()).isEqualTo(1);
        verify(cartMapper).insert(any());
    }

    @Test
    void addItem_existingItem_incrementsQuantity() {
        Cart existing = new Cart();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setProductId(10L);
        existing.setQuantity(3);
        when(cartMapper.selectOne(any())).thenReturn(existing);
        when(cartMapper.updateById(any())).thenReturn(1);

        Cart result = cartService.addItem(1L, 10L, 2);

        assertThat(result.getQuantity()).isEqualTo(5);
        verify(cartMapper).updateById(any());
    }

    @Test
    void removeItem_success() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        when(cartMapper.selectById(1L)).thenReturn(cart);
        when(cartMapper.deleteById(1L)).thenReturn(1);

        cartService.removeItem(1L, 1L);
        verify(cartMapper).deleteById(1L);
    }

    @Test
    void removeItem_notFound_throwsBizException() {
        when(cartMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> cartService.removeItem(1L, 99L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40030);
    }

    @Test
    void removeItem_wrongUser_throwsBizException() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(2L);
        when(cartMapper.selectById(1L)).thenReturn(cart);

        assertThatThrownBy(() -> cartService.removeItem(1L, 1L))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40030);
    }

    @Test
    void updateQuantity_success() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setQuantity(2);
        when(cartMapper.selectById(1L)).thenReturn(cart);
        when(cartMapper.updateById(any())).thenReturn(1);

        cartService.updateQuantity(1L, 1L, 5);

        verify(cartMapper).updateById(argThat(c -> ((Cart) c).getQuantity() == 5));
    }

    @Test
    void updateQuantity_notFound_throwsBizException() {
        when(cartMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> cartService.updateQuantity(1L, 99L, 5))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40030);
    }

    @Test
    void removeByIds_success() {
        when(cartMapper.delete(any())).thenReturn(2);

        cartService.removeByIds(1L, List.of(1L, 2L));
        verify(cartMapper).delete(any());
    }

    @Test
    void getSelectedItems_returnsFiltered() {
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartMapper.selectList(any())).thenReturn(List.of(cart));

        List<Cart> result = cartService.getSelectedItems(1L, List.of(1L));
        assertThat(result).hasSize(1);
    }
}
