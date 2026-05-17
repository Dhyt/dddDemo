package com.ddd.payment.domain.event;

import com.ddd.common.domain.DomainEvent;
import com.ddd.payment.domain.model.PaymentId;
import java.time.Instant;

public record PaymentSucceededEvent(PaymentId paymentId, Long orderId, Instant occurredAt) implements DomainEvent {
    public PaymentSucceededEvent(PaymentId paymentId, Long orderId) {
        this(paymentId, orderId, Instant.now());
    }
}
