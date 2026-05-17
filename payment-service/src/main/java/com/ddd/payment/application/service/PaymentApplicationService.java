package com.ddd.payment.application.service;

import com.ddd.payment.application.dto.CreatePaymentRequest;
import com.ddd.payment.application.dto.PaymentResponse;
import com.ddd.payment.application.dto.PaymentResponse.TransactionResponse;
import com.ddd.payment.domain.model.Payment;
import com.ddd.payment.domain.model.PaymentId;
import com.ddd.payment.domain.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentApplicationService {

    private final PaymentRepository paymentRepository;

    public PaymentApplicationService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse createPayment(CreatePaymentRequest request) {
        Payment payment = new Payment(request.orderId(), request.amount());
        payment.complete();
        paymentRepository.save(payment);
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        Payment payment = paymentRepository.findById(new PaymentId(id))
                .orElseThrow(() -> new IllegalArgumentException("支付记录不存在: " + id));
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("订单支付记录不存在: " + orderId));
        return toResponse(payment);
    }

    public PaymentResponse refundPayment(Long id) {
        Payment payment = paymentRepository.findById(new PaymentId(id))
                .orElseThrow(() -> new IllegalArgumentException("支付记录不存在: " + id));
        payment.refund();
        paymentRepository.save(payment);
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId().value(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getStatus().name(),
                payment.getTransactions().stream()
                        .map(t -> new TransactionResponse(
                                t.getTransactionId() != null ? t.getTransactionId().value() : null,
                                t.getType().name(),
                                t.getAmount(),
                                t.getStatus().name(),
                                t.getCreatedAt()
                        ))
                        .toList(),
                payment.getCreatedAt()
        );
    }
}
