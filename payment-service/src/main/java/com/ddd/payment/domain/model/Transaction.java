package com.ddd.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 交易流水实体 — 记录每笔支付/退款操作。
 *
 * DDD: Entity — 有唯一标识，记录不可修改，只能追加。
 */
public class Transaction {
    public enum TransactionType { PAYMENT, REFUND }
    public enum TransactionStatus { SUCCESS, FAILED }

    private TransactionId transactionId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final TransactionStatus status;
    private final Instant createdAt;

    public Transaction(TransactionType type, BigDecimal amount, TransactionStatus status) {
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public TransactionId getTransactionId() { return transactionId; }
    public void setTransactionId(TransactionId id) { this.transactionId = id; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public TransactionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
