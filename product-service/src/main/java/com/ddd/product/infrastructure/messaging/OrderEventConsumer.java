package com.ddd.product.infrastructure.messaging;

import com.ddd.common.event.OrderCancelledMessage;
import com.ddd.common.event.OrderSubmittedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单事件消费者 — 商品服务监听订单提交/取消事件，用于库存预留或释放。
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.ORDER_SUBMITTED_QUEUE)
    public void handleOrderSubmitted(OrderSubmittedMessage message) {
        log.info("[ProductService] Received order submitted event: orderId={}, customerId={}, totalAmount={} {}",
                message.orderId(), message.customerId(), message.totalAmount(), message.currency());
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_CANCELLED_QUEUE)
    public void handleOrderCancelled(OrderCancelledMessage message) {
        log.info("[ProductService] Received order cancelled event: orderId={}", message.orderId());
    }
}
