package com.mall.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.order.dto.CreateOrderRequest;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.mapper.OrderItemMapper;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.mq.OrderEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private OrderEventPublisher eventPublisher;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);

        CreateOrderRequest request = buildCreateOrderRequest();

        Order result = orderService.createOrder(1L, request);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
        verify(eventPublisher).publishOrderCreated(any(Order.class));
    }

    @Test
    void createOrder_duplicateSubmission_throwsBizException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(anyString(), anyString(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        CreateOrderRequest request = buildCreateOrderRequest();

        assertThatThrownBy(() -> orderService.createOrder(1L, request))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40050);
    }

    @Test
    void listUserOrders_withoutStatusFilter() {
        Page<Order> page = new Page<>(1, 10);
        page.setRecords(List.of(new Order()));
        page.setTotal(1);
        when(orderMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<Order> result = orderService.listUserOrders(1L, null, 1, 10);
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void listUserOrders_withStatusFilter() {
        Page<Order> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);
        when(orderMapper.selectPage(any(), any())).thenReturn(page);

        PageResult<Order> result = orderService.listUserOrders(1L, 1, 1, 10);
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getByOrderNo_returnsOrder() {
        Order order = new Order();
        order.setOrderNo("ORD001");
        when(orderMapper.selectOne(any())).thenReturn(order);

        Order result = orderService.getByOrderNo("ORD001");
        assertThat(result.getOrderNo()).isEqualTo("ORD001");
    }

    @Test
    void getOrderItems_returnsList() {
        OrderItem item = new OrderItem();
        item.setOrderId(1L);
        when(orderItemMapper.selectList(any())).thenReturn(List.of(item));

        List<OrderItem> result = orderService.getOrderItems(1L);
        assertThat(result).hasSize(1);
    }

    @Test
    void markPaid_statusZero_updatesAndPublishes() {
        Order order = new Order();
        order.setOrderNo("ORD001");
        order.setStatus(0);
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.updateById(any())).thenReturn(1);

        orderService.markPaid("ORD001", "alipay");

        assertThat(order.getStatus()).isEqualTo(1);
        assertThat(order.getPaymentMethod()).isEqualTo("alipay");
        verify(eventPublisher).publishOrderPaid(order);
    }

    @Test
    void markPaid_orderNotFound_doesNothing() {
        when(orderMapper.selectOne(any())).thenReturn(null);
        orderService.markPaid("NONE", "alipay");
        verify(orderMapper, never()).updateById(any());
    }

    @Test
    void markPaid_nonZeroStatus_doesNothing() {
        Order order = new Order();
        order.setStatus(1);
        when(orderMapper.selectOne(any())).thenReturn(order);

        orderService.markPaid("ORD001", "alipay");
        verify(orderMapper, never()).updateById(any());
    }

    @Test
    void cancelOrder_success() {
        Order order = new Order();
        order.setOrderNo("ORD001");
        order.setUserId(1L);
        order.setStatus(0);
        when(orderMapper.selectOne(any())).thenReturn(order);
        when(orderMapper.updateById(any())).thenReturn(1);

        orderService.cancelOrder(1L, "ORD001");

        assertThat(order.getStatus()).isEqualTo(5);
        verify(eventPublisher).publishOrderCancelled(order);
    }

    @Test
    void cancelOrder_wrongUser_throwsBizException() {
        Order order = new Order();
        order.setUserId(2L);
        order.setStatus(0);
        when(orderMapper.selectOne(any())).thenReturn(order);

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "ORD001"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40051);
    }

    @Test
    void cancelOrder_nonZeroStatus_throwsBizException() {
        Order order = new Order();
        order.setUserId(1L);
        order.setStatus(1);
        when(orderMapper.selectOne(any())).thenReturn(order);

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "ORD001"))
                .isInstanceOf(BizException.class)
                .extracting("code").isEqualTo(40051);
    }

    private CreateOrderRequest buildCreateOrderRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setIdempotencyKey("unique-key-1");
        request.setReceiverName("John");
        request.setReceiverPhone("13800138000");
        request.setReceiverAddress("Address");

        CreateOrderRequest.OrderItemDTO item = new CreateOrderRequest.OrderItemDTO();
        item.setProductId(1L);
        item.setProductName("Phone");
        item.setPrice(new BigDecimal("100.00"));
        item.setQuantity(2);
        item.setMerchantId(1L);
        request.setItems(List.of(item));

        return request;
    }
}
