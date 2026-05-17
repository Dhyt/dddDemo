package com.ddd.order.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.order.domain.model.OrderId;
import java.time.Instant;

public record OrderCancelledEvent(OrderId orderId, Instant occurredAt) implements DomainEvent {
    public OrderCancelledEvent(OrderId orderId) {
        this(orderId, Instant.now());
    }
}
