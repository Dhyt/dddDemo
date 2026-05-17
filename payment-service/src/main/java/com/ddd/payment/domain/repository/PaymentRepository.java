package com.ddd.payment.domain.repository;

import com.ddd.payment.domain.model.Payment;
import com.ddd.payment.domain.model.PaymentId;
import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(PaymentId id);
    void save(Payment payment);
    Optional<Payment> findByOrderId(Long orderId);
}
