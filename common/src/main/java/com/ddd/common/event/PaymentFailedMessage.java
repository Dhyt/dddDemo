package com.ddd.common.event;

public record PaymentFailedMessage(Long paymentId, Long orderId, String reason) {}
