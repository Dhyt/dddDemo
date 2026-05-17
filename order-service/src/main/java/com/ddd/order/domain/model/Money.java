package com.ddd.order.domain.model;

import com.ddd.common.domain.ValueObject;
import com.ddd.common.exception.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金额值对象 — 不可变，包含金额和币种。
 *
 * DDD: Value Object — 通过属性值定义自身，无唯一标识，不可变。
 *      所有金额操作必须通过对象方法完成，确保币种一致性。
 */
public record Money(BigDecimal amount, String currency) implements ValueObject {

    public Money {
        if (amount == null || currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("金额和币种不能为空");
        }
        // 防御性拷贝 — record compact constructor
        amount = new BigDecimal(amount.toPlainString()).setScale(2, RoundingMode.HALF_UP);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainException("CURRENCY_MISMATCH",
                    "不同币种不可相加: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainException("CURRENCY_MISMATCH",
                    "不同币种不可相减: " + this.currency + " vs " + other.currency);
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(int quantity) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(quantity)), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new DomainException("CURRENCY_MISMATCH", "不同币种不可比较");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }
}
