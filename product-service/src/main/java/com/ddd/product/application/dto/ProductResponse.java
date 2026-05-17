package com.ddd.product.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        int availableQuantity,
        int reservedQuantity,
        String status,
        Instant createdAt
) {}
