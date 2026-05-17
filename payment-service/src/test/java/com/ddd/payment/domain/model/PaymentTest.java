package com.ddd.payment.domain.model;

import com.ddd.common.exception.DomainException;
import com.ddd.payment.domain.event.PaymentFailedEvent;
import com.ddd.payment.domain.event.PaymentRefundedEvent;
import com.ddd.payment.domain.event.PaymentSucceededEvent;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class PaymentTest {

    @Test
    void shouldInitiatePayment() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        assertEquals(PaymentStatus.INITIATED, payment.getStatus());
    }

    @Test
    void shouldCompletePayment() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        PaymentSucceededEvent event = payment.complete();
        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        assertNotNull(event);
    }

    @Test
    void shouldFailPayment() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        PaymentFailedEvent event = payment.fail("余额不足");
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("余额不足", event.reason());
    }

    @Test
    void shouldRefundOnlySucceededPayment() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        payment.complete();
        PaymentRefundedEvent event = payment.refund();
        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
    }

    @Test
    void shouldNotRefundFailedPayment() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        payment.fail("余额不足");
        assertThrows(DomainException.class, payment::refund);
    }

    @Test
    void shouldTrackTransactions() {
        Payment payment = new Payment(1L, new BigDecimal("100.00"));
        payment.complete();
        payment.refund();
        assertEquals(2, payment.getTransactions().size());
    }
}
