package com.ddd.product.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.product.domain.model.ProductId;
import com.ddd.product.domain.model.Stock;
import java.time.Instant;

public record StockChangedEvent(
        ProductId productId,
        int quantityChange,
        Stock currentStock,
        Instant occurredAt
) implements DomainEvent {
    public StockChangedEvent(ProductId productId, int quantityChange, Stock currentStock) {
        this(productId, quantityChange, currentStock, Instant.now());
    }
}
