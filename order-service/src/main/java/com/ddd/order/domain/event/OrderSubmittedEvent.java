package com.ddd.order.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.order.domain.model.CustomerId;
import com.ddd.order.domain.model.Money;
import com.ddd.order.domain.model.OrderId;
import java.time.Instant;

public record OrderSubmittedEvent(
        OrderId orderId,
        CustomerId customerId,
        Money totalAmount,
        Instant occurredAt
) implements DomainEvent {
}
