package com.ddd.order.infrastructure.messaging;

import com.ddd.common.event.OrderCancelledMessage;
import com.ddd.common.event.OrderSubmittedMessage;
import com.ddd.order.domain.event.OrderCancelledEvent;
import com.ddd.order.domain.event.OrderSubmittedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 订单事件发布器 — 将订单领域事件转换为跨服务消息并发送到 RabbitMQ。
 */
@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OrderSubmittedEvent event) {
        OrderSubmittedMessage message = new OrderSubmittedMessage(
                event.orderId().value(),
                event.customerId().value(),
                event.totalAmount().amount(),
                event.totalAmount().currency()
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_SUBMITTED_RK,
                message
        );
    }

    public void publish(OrderCancelledEvent event) {
        OrderCancelledMessage message = new OrderCancelledMessage(
                event.orderId().value()
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CANCELLED_RK,
                message
        );
    }
}
