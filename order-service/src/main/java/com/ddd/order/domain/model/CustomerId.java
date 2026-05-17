package com.ddd.order.domain.model;

import com.ddd.common.domain.ValueObject;

public record CustomerId(Long value) implements ValueObject {
    public CustomerId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("客户ID必须为正数");
        }
    }
}
