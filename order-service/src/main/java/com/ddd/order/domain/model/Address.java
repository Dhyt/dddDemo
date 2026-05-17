package com.ddd.order.domain.model;

import com.ddd.common.domain.ValueObject;

public record Address(String street, String city, String zipCode) implements ValueObject {
    public Address {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("街道地址不能为空");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("城市不能为空");
        }
    }
}
