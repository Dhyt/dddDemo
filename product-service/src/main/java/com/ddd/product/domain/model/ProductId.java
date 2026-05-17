package com.ddd.product.domain.model;

import com.ddd.common.domain.ValueObject;

public record ProductId(Long value) implements ValueObject {
    public ProductId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("商品ID必须为正数");
        }
    }
}
