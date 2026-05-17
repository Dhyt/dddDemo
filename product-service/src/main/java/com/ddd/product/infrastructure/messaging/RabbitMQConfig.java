package com.ddd.product.infrastructure.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置 — 商品服务监听订单事件。
 */
@Configuration
public class RabbitMQConfig {

    public static final String ORDER_EXCHANGE = "ddd.order.events";

    public static final String ORDER_SUBMITTED_QUEUE = "product.order.submitted.queue";
    public static final String ORDER_CANCELLED_QUEUE = "product.order.cancelled.queue";

    public static final String ORDER_SUBMITTED_RK = "order.submitted";
    public static final String ORDER_CANCELLED_RK = "order.cancelled";

    // ---- Exchanges ----

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
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

    // ---- Bindings ----

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
}
