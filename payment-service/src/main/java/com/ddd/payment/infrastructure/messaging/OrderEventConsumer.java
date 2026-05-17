package com.ddd.payment.infrastructure.messaging;

import com.ddd.common.event.OrderSubmittedMessage;
import com.ddd.payment.application.dto.CreatePaymentRequest;
import com.ddd.payment.application.service.PaymentApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单事件消费者 — 支付服务监听订单提交事件，自动创建并完成支付。
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final PaymentApplicationService paymentApplicationService;

    public OrderEventConsumer(PaymentApplicationService paymentApplicationService) {
        this.paymentApplicationService = paymentApplicationService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_SUBMITTED_QUEUE)
    public void handleOrderSubmitted(OrderSubmittedMessage message) {
        log.info("[PaymentService] Received order submitted event: orderId={}, customerId={}, totalAmount={} {}",
                message.orderId(), message.customerId(), message.totalAmount(), message.currency());

        // Simplified: auto-complete payment immediately
        paymentApplicationService.createPayment(
                new CreatePaymentRequest(message.orderId(), message.totalAmount())
        );
        log.info("[PaymentService] Payment auto-completed for order: {}", message.orderId());
    }
}
