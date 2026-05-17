package com.ddd.order.domain.model;

/**
 * 订单状态值对象 — 枚举订单生命周期中的所有状态。
 *
 * DDD: 状态用枚举作为 Value Object，表示订单在其生命周期中可能处于的阶段。
 */
public enum OrderStatus {
    PENDING,      // 待处理
    PAID,         // 已支付
    SHIPPED,      // 已发货
    DELIVERED,    // 已签收
    CANCELLED     // 已取消
}
