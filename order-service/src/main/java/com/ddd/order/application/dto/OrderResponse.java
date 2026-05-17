package com.ddd.order.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long orderId,
        Long customerId,
        BigDecimal totalAmount,
        String currency,
        String status,
        List<OrderItemResponse> items,
        Instant createdAt
) {
    public record OrderItemResponse(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}
}
