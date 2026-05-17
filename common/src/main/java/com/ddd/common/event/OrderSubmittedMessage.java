package com.ddd.common.event;

import java.math.BigDecimal;

/**
 * 跨服务共享的订单提交消息 — 通过 RabbitMQ 序列化传输。
 * 注意：这不是领域事件！领域事件定义在每个服务的 Domain 层。
 * 这是 Application 层的事件消息 DTO，用于跨限界上下文通讯。
 */
public record OrderSubmittedMessage(
        Long orderId,
        Long customerId,
        BigDecimal totalAmount,
        String currency
) {}
