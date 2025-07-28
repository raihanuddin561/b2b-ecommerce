package com.dealkartbd.backend_app.dto.orders;

import lombok.*;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "Seller company ID is required")
    private Long sellerCompanyId;

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> orderItems;

    @Size(max = 500, message = "Shipping address cannot exceed 500 characters")
    private String shippingAddress;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    private BigDecimal shippingCost;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
