package com.ddd.order.infrastructure.messaging;

import com.ddd.common.event.PaymentFailedMessage;
import com.ddd.common.event.PaymentSucceededMessage;
import com.ddd.order.application.service.OrderApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 支付事件消费者 — 监听支付服务发送的支付成功/失败消息并更新订单状态。
 */
@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final OrderApplicationService orderApplicationService;

    public PaymentEventConsumer(OrderApplicationService orderApplicationService) {
        this.orderApplicationService = orderApplicationService;
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_SUCCEEDED_ORDER_QUEUE)
    public void handlePaymentSucceeded(PaymentSucceededMessage message) {
        log.info("Received payment succeeded event: paymentId={}, orderId={}",
                message.paymentId(), message.orderId());
        orderApplicationService.payOrder(message.orderId());
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_FAILED_ORDER_QUEUE)
    public void handlePaymentFailed(PaymentFailedMessage message) {
        log.warn("Received payment failed event: paymentId={}, orderId={}, reason={}",
                message.paymentId(), message.orderId(), message.reason());
    }
}
