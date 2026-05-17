package com.ddd.payment.domain.model;

import com.ddd.common.domain.ValueObject;

public record PaymentId(Long value) implements ValueObject {
    public PaymentId {
        if (value == null || value <= 0) throw new IllegalArgumentException("支付ID必须为正数");
    }
}
