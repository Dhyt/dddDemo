package com.ddd.order.domain.model;

import com.ddd.common.domain.AggregateRoot;
import com.ddd.common.exception.DomainException;
import com.ddd.order.domain.event.OrderCancelledEvent;
import com.ddd.order.domain.event.OrderDeliveredEvent;
import com.ddd.order.domain.event.OrderPaidEvent;
import com.ddd.order.domain.event.OrderSubmittedEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 订单聚合根 — 订单管理的核心入口。
 *
 * DDD: Aggregate Root — 外部只能通过 Order 对象操作订单项。
 *      所有修改都必须通过 Order 的方法，确保业务规则（不变量）始终成立。
 *      聚合根方法返回领域事件，由 Application 层发布到消息队列。
 *
 * 业务规则:
 * 1. 订单必须至少包含 1 个 OrderItem
 * 2. 只有 PENDING 状态可以取消
 * 3. 只有 PENDING 状态可以标记支付
 * 4. 总金额 = 所有 OrderItem 金额之和
 */
public class Order extends AggregateRoot<OrderId> {

    private CustomerId customerId;
    private final List<OrderItem> items;
    private Money totalAmount;
    private OrderStatus status;
    private boolean submitted;
    private Address shippingAddress;
    private Instant createdAt;

    public Order(CustomerId customerId, Address shippingAddress) {
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.totalAmount = new Money(java.math.BigDecimal.ZERO, "CNY");
        this.status = OrderStatus.PENDING;
        this.submitted = false;
        this.shippingAddress = shippingAddress;
        this.createdAt = Instant.now();
    }

    /** 添加订单项 */
    public void addItem(ProductId productId, String productName, int quantity, Money unitPrice) {
        if (submitted || this.status != OrderStatus.PENDING) {
            throw new DomainException("ORDER_NOT_MODIFIABLE", "订单已提交，不可修改商品");
        }
        OrderItem item = new OrderItem(productId, productName, quantity, unitPrice);
        this.items.add(item);
        this.totalAmount = this.totalAmount.add(item.getSubtotal());
    }

    /** 提交订单 — 触发 OrderSubmittedEvent */
    public OrderSubmittedEvent submit() {
        if (items.isEmpty()) {
            throw new DomainException("ORDER_EMPTY", "订单必须包含至少一个商品");
        }
        this.submitted = true;
        return new OrderSubmittedEvent(getId(), this.customerId, this.totalAmount, this.createdAt);
    }

    /** 标记已支付 */
    public OrderPaidEvent markPaid() {
        if (this.status != OrderStatus.PENDING) {
            throw new DomainException("INVALID_STATUS",
                    "只能支付待处理的订单，当前状态: " + this.status);
        }
        this.status = OrderStatus.PAID;
        return new OrderPaidEvent(getId());
    }

    /** 取消订单 */
    public OrderCancelledEvent cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new DomainException("INVALID_STATUS",
                    "只能取消待处理的订单，当前状态: " + this.status);
        }
        this.status = OrderStatus.CANCELLED;
        return new OrderCancelledEvent(getId());
    }

    /** 标记已发货 */
    public void markShipped() {
        if (this.status != OrderStatus.PAID) {
            throw new DomainException("INVALID_STATUS",
                    "只能发货已支付的订单，当前状态: " + this.status);
        }
        this.status = OrderStatus.SHIPPED;
    }

    /** 标记已签收 */
    public OrderDeliveredEvent markDelivered() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new DomainException("INVALID_STATUS",
                    "只能签收已发货的订单，当前状态: " + this.status);
        }
        this.status = OrderStatus.DELIVERED;
        return new OrderDeliveredEvent(getId());
    }

    // Getters — 只读暴露
    public CustomerId getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public Money getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public Address getShippingAddress() { return shippingAddress; }
    public Instant getCreatedAt() { return createdAt; }
}
