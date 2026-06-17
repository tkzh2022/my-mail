package com.mall.cart.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.cart.entity.Cart;
import com.mall.cart.mapper.CartMapper;
import com.mall.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;

    public List<Cart> getUserCart(Long userId) {
        return cartMapper.selectList(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId).orderByDesc(Cart::getCreatedAt));
    }

    public Cart addItem(Long userId, Long productId, int quantity) {
        Cart existing = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId).eq(Cart::getProductId, productId));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartMapper.updateById(existing);
            return existing;
        }
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        cart.setSelected(1);
        cartMapper.insert(cart);
        return cart;
    }

    public void removeItem(Long userId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BizException(40030, "Cart item not found");
        }
        cartMapper.deleteById(cartId);
    }

    public void updateQuantity(Long userId, Long cartId, int quantity) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BizException(40030, "Cart item not found");
        }
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
    }

    public void removeByIds(Long userId, List<Long> cartIds) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId).in(Cart::getId, cartIds));
    }

    public List<Cart> getSelectedItems(Long userId, List<Long> cartItemIds) {
        return cartMapper.selectList(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId).in(Cart::getId, cartItemIds));
    }
}
