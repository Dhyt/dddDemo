package com.ddd.order.domain.repository;

import com.ddd.order.domain.model.Order;
import com.ddd.order.domain.model.OrderId;
import java.util.List;
import java.util.Optional;

/**
 * 订单仓储接口 — 定义在 Domain 层，实现在 Infrastructure 层。
 *
 * DDD: Repository 接口属于 Domain 层，因为它是聚合持久化的抽象。
 *      实现细节（JPA/MyBatis/etc）对领域层不可见。
 */
public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    void save(Order order);
    List<Order> findAll();
}
