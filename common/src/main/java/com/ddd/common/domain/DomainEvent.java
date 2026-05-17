package com.ddd.common.domain;

import java.time.Instant;

/**
 * 领域事件接口 — 所有领域事件的父接口。
 *
 * DDD: 领域事件表示领域中发生的、对业务有意义的过去事件。
 *      命名使用过去时态（如 OrderSubmittedEvent），因为事件已经发生不可更改。
 *      领域事件是实现限界上下文解耦的关键手段。
 */
public interface DomainEvent {
    /** 事件发生的时间戳 */
    Instant occurredAt();
}
