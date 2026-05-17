package com.ddd.payment.application.dto;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        Long orderId,
        BigDecimal amount
) {}
