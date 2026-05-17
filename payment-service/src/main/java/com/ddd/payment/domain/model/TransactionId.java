package com.ddd.payment.domain.model;

import com.ddd.common.domain.ValueObject;

public record TransactionId(Long value) implements ValueObject {
    public TransactionId {
        if (value == null || value <= 0) throw new IllegalArgumentException("交易ID必须为正数");
    }
}
