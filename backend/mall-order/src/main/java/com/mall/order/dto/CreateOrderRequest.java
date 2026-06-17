package com.mall.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    @NotBlank
    private String idempotencyKey;
    @NotBlank
    private String receiverName;
    @NotBlank
    private String receiverPhone;
    @NotBlank
    private String receiverAddress;
    private String remark;
    @NotEmpty
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long productId;
        private String productName;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private Long merchantId;
    }
}
