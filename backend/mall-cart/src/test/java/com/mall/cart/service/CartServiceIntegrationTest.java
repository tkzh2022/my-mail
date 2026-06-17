package com.mall.cart.service;

import com.mall.cart.entity.Cart;
import com.mall.common.exception.BizException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Test
    void addItem_andGetCart_fullFlow() {
        Cart added = cartService.addItem(1L, 100L, 2);
        assertThat(added.getId()).isNotNull();
        assertThat(added.getQuantity()).isEqualTo(2);

        List<Cart> cart = cartService.getUserCart(1L);
        assertThat(cart).hasSize(1);
        assertThat(cart.get(0).getProductId()).isEqualTo(100L);
    }

    @Test
    void addItem_existingProduct_incrementsQuantity() {
        cartService.addItem(1L, 100L, 2);
        Cart updated = cartService.addItem(1L, 100L, 3);

        assertThat(updated.getQuantity()).isEqualTo(5);

        List<Cart> cart = cartService.getUserCart(1L);
        assertThat(cart).hasSize(1);
    }

    @Test
    void removeItem_success() {
        Cart added = cartService.addItem(1L, 100L, 1);
        cartService.removeItem(1L, added.getId());

        List<Cart> cart = cartService.getUserCart(1L);
        assertThat(cart).isEmpty();
    }

    @Test
    void removeItem_wrongUser_throwsException() {
        Cart added = cartService.addItem(1L, 100L, 1);

        assertThatThrownBy(() -> cartService.removeItem(2L, added.getId()))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40030);
    }

    @Test
    void updateQuantity_success() {
        Cart added = cartService.addItem(1L, 100L, 2);
        cartService.updateQuantity(1L, added.getId(), 10);

        List<Cart> cart = cartService.getUserCart(1L);
        assertThat(cart.get(0).getQuantity()).isEqualTo(10);
    }

    @Test
    void getSelectedItems_filtersCorrectly() {
        Cart item1 = cartService.addItem(1L, 100L, 1);
        Cart item2 = cartService.addItem(1L, 200L, 2);

        List<Cart> selected = cartService.getSelectedItems(1L, List.of(item1.getId()));
        assertThat(selected).hasSize(1);
        assertThat(selected.get(0).getProductId()).isEqualTo(100L);
    }
}
