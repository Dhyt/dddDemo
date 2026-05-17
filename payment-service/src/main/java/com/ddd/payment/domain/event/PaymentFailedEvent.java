package com.ddd.payment.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.payment.domain.model.PaymentId;
import java.time.Instant;

public record PaymentFailedEvent(PaymentId paymentId, Long orderId, String reason, Instant occurredAt) implements DomainEvent {
    public PaymentFailedEvent(PaymentId paymentId, Long orderId, String reason) {
        this(paymentId, orderId, reason, Instant.now());
    }
}
