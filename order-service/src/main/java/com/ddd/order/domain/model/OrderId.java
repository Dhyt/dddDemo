package com.ddd.order.domain.model;

import com.ddd.common.domain.ValueObject;

public record OrderId(Long value) implements ValueObject {
    public OrderId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("订单ID必须为正数");
        }
    }
}
