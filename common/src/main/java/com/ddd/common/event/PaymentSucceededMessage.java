package com.ddd.common.event;

public record PaymentSucceededMessage(Long paymentId, Long orderId) {}
