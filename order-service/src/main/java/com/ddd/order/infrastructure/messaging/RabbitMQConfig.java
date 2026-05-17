package com.ddd.order.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置 — 定义交换器、队列和绑定关系。
 *
 * 订单服务发布订单事件到 ddd.order.events，同时消费来自 ddd.payment.events 的支付结果事件。
 */
@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "ddd.order.events";
    public static final String PAYMENT_EXCHANGE = "ddd.payment.events";

    public static final String ORDER_SUBMITTED_QUEUE = "order.submitted.queue";
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";
    public static final String PAYMENT_SUCCEEDED_ORDER_QUEUE = "payment.succeeded.order.queue";
    public static final String PAYMENT_FAILED_ORDER_QUEUE = "payment.failed.order.queue";

    public static final String ORDER_SUBMITTED_RK = "order.submitted";
    public static final String ORDER_CANCELLED_RK = "order.cancelled";
    public static final String PAYMENT_SUCCEEDED_RK = "payment.succeeded";
    public static final String PAYMENT_FAILED_RK = "payment.failed";

    // ---- Exchanges ----

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    // ---- Queues ----

    @Bean
    public Queue orderSubmittedQueue() {
        return new Queue(ORDER_SUBMITTED_QUEUE);
    }

    @Bean
    public Queue orderCancelledQueue() {
        return new Queue(ORDER_CANCELLED_QUEUE);
    }

    @Bean
    public Queue paymentSucceededOrderQueue() {
        return new Queue(PAYMENT_SUCCEEDED_ORDER_QUEUE);
    }

    @Bean
    public Queue paymentFailedOrderQueue() {
        return new Queue(PAYMENT_FAILED_ORDER_QUEUE);
    }

    // ---- Bindings for Order Events (to ddd.order.events) ----

    @Bean
    public Binding orderSubmittedBinding() {
        return BindingBuilder.bind(orderSubmittedQueue())
                .to(orderExchange())
                .with(ORDER_SUBMITTED_RK);
    }

    @Bean
    public Binding orderCancelledBinding() {
        return BindingBuilder.bind(orderCancelledQueue())
                .to(orderExchange())
                .with(ORDER_CANCELLED_RK);
    }

    // ---- Bindings for Payment Events (from ddd.payment.events) ----

    @Bean
    public Binding paymentSucceededOrderBinding() {
        return BindingBuilder.bind(paymentSucceededOrderQueue())
                .to(paymentExchange())
                .with(PAYMENT_SUCCEEDED_RK);
    }

    @Bean
    public Binding paymentFailedOrderBinding() {
        return BindingBuilder.bind(paymentFailedOrderQueue())
                .to(paymentExchange())
                .with(PAYMENT_FAILED_RK);
    }
}
