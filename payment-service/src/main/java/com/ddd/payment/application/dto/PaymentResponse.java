package com.ddd.payment.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PaymentResponse(
        Long paymentId,
        Long orderId,
        BigDecimal amount,
        String status,
        List<TransactionResponse> transactions,
        Instant createdAt
) {
    public record TransactionResponse(
            Long transactionId,
            String type,
            BigDecimal amount,
            String status,
            Instant createdAt
    ) {}
}
