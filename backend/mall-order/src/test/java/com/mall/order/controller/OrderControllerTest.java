package com.mall.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.exception.BizException;
import com.mall.common.result.PageResult;
import com.mall.order.entity.Order;
import com.mall.order.service.OrderService;
import com.mall.order.service.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;
    @Mock
    private RefundService refundService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void listOrders_returnsPageResult() throws Exception {
        PageResult<Order> pageResult = new PageResult<>();
        Order order = new Order();
        order.setId(1L);
        order.setOrderNo("ORD001");
        order.setTotalAmount(new BigDecimal("199.99"));
        pageResult.setItems(List.of(order));
        pageResult.setTotal(1);
        pageResult.setPage(1);
        pageResult.setSize(10);
        when(orderService.listUserOrders(eq(1L), isNull(), eq(1), eq(10))).thenReturn(pageResult);

        mockMvc.perform(get("/api/orders")
                        .header("X-User-Id", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.items[0].orderNo").value("ORD001"));
    }

    @Test
    void cancelOrder_success() throws Exception {
        doNothing().when(orderService).cancelOrder(1L, "ORD001");

        mockMvc.perform(put("/api/orders/ORD001/cancel")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
