package com.ddd.common.domain;

/**
 * 聚合根基类 — 所有聚合根的父类。
 *
 * DDD: 聚合根是聚合的入口，外部只能通过聚合根访问聚合内的实体。
 *      聚合根负责保证其内部所有业务规则（不变量）的一致性。
 */
public abstract class AggregateRoot<T> {
    private T id;

    public T getId() {
        return id;
    }

    protected void setId(T id) {
        this.id = id;
    }
}
