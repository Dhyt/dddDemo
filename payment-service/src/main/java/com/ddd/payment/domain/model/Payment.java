package com.ddd.payment.domain.model;

import com.ddd.common.domain.AggregateRoot;
import com.ddd.common.exception.DomainException;
import com.ddd.payment.domain.event.PaymentFailedEvent;
import com.ddd.payment.domain.event.PaymentRefundedEvent;
import com.ddd.payment.domain.event.PaymentSucceededEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 支付聚合根 — 处理支付生命周期。
 *
 * DDD: Aggregate Root — 支付操作必须通过聚合根方法。
 *
 * 业务规则:
 * 1. 一笔支付对应一个订单
 * 2. 支付成功后才能退款
 * 3. 交易流水不可修改，只能追加
 */
public class Payment extends AggregateRoot<PaymentId> {

    private Long orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private final List<Transaction> transactions;
    private Instant createdAt;

    public Payment(Long orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.INITIATED;
        this.transactions = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    public PaymentSucceededEvent complete() {
        if (this.status != PaymentStatus.INITIATED && this.status != PaymentStatus.PROCESSING) {
            throw new DomainException("INVALID_STATUS",
                    "当前支付状态不可完成: " + this.status);
        }
        this.status = PaymentStatus.SUCCEEDED;
        this.transactions.add(new Transaction("PAYMENT", amount, "SUCCESS"));
        return new PaymentSucceededEvent(getId(), this.orderId);
    }

    public PaymentFailedEvent fail(String reason) {
        if (this.status == PaymentStatus.SUCCEEDED) {
            throw new DomainException("INVALID_STATUS", "已成功的支付不可标记失败");
        }
        this.status = PaymentStatus.FAILED;
        this.transactions.add(new Transaction("PAYMENT", amount, "FAILED"));
        return new PaymentFailedEvent(getId(), this.orderId, reason);
    }

    public PaymentRefundedEvent refund() {
        if (this.status != PaymentStatus.SUCCEEDED) {
            throw new DomainException("INVALID_STATUS",
                    "只能退款已成功的支付，当前状态: " + this.status);
        }
        this.status = PaymentStatus.REFUNDED;
        this.transactions.add(new Transaction("REFUND", amount, "SUCCESS"));
        return new PaymentRefundedEvent(getId(), this.orderId);
    }

    public void setMethod(PaymentMethod method) { this.method = method; }

    // Getters
    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public PaymentMethod getMethod() { return method; }
    public List<Transaction> getTransactions() { return Collections.unmodifiableList(transactions); }
    public Instant getCreatedAt() { return createdAt; }
}
