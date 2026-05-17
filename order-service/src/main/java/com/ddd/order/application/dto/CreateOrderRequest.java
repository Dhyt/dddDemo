package com.ddd.order.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
        Long customerId,
        String street,
        String city,
        String zipCode,
        List<OrderItemRequest> items
) {
    public record OrderItemRequest(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice
    ) {}
}
