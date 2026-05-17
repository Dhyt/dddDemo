package com.ddd.payment.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置 — 支付服务监听订单事件，并发布支付结果事件。
 */
@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "ddd.order.events";
    public static final String PAYMENT_EXCHANGE = "ddd.payment.events";

    public static final String ORDER_SUBMITTED_QUEUE = "payment.order.submitted.queue";
    public static final String PAYMENT_SUCCEEDED_QUEUE = "payment.succeeded.queue";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";

    public static final String ORDER_SUBMITTED_RK = "order.submitted";
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
    public Queue paymentSucceededQueue() {
        return new Queue(PAYMENT_SUCCEEDED_QUEUE);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(PAYMENT_FAILED_QUEUE);
    }

    // ---- Bindings ----

    @Bean
    public Binding orderSubmittedBinding() {
        return BindingBuilder.bind(orderSubmittedQueue())
                .to(orderExchange())
                .with(ORDER_SUBMITTED_RK);
    }

    @Bean
    public Binding paymentSucceededBinding() {
        return BindingBuilder.bind(paymentSucceededQueue())
                .to(paymentExchange())
                .with(PAYMENT_SUCCEEDED_RK);
    }

    @Bean
    public Binding paymentFailedBinding() {
        return BindingBuilder.bind(paymentFailedQueue())
                .to(paymentExchange())
                .with(PAYMENT_FAILED_RK);
    }
}
