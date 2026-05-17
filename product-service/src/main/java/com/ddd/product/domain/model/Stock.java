package com.ddd.product.domain.model;

import com.ddd.common.domain.ValueObject;

/**
 * 库存值对象 — 跟踪可用和预留数量。
 *
 * DDD: Value Object — 封装库存数量的业务逻辑，避免原始类型滥用。
 *      availableQuantity: 实际库存
 *      reservedQuantity: 已预留但未出库的数量
 *      实际可销售数量 = availableQuantity - reservedQuantity
 */
public record Stock(int availableQuantity, int reservedQuantity) implements ValueObject {
    public Stock {
        if (availableQuantity < 0 || reservedQuantity < 0) {
            throw new IllegalArgumentException("库存数量不能为负数");
        }
        if (reservedQuantity > availableQuantity) {
            throw new IllegalArgumentException("预留数量不能超过可用数量");
        }
    }

    public Stock reserve(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("预留数量必须为正数");
        if (reservedQuantity + quantity > availableQuantity) {
            throw new IllegalArgumentException("预留数量超过可用库存");
        }
        return new Stock(availableQuantity, reservedQuantity + quantity);
    }

    public Stock release(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("释放数量必须为正数");
        if (reservedQuantity < quantity) {
            throw new IllegalArgumentException("释放数量超过预留数量");
        }
        return new Stock(availableQuantity, reservedQuantity - quantity);
    }
}
