package com.ddd.order.infrastructure.persistence;

import com.ddd.common.domain.AggregateRoot;
import com.ddd.order.domain.model.*;
import com.ddd.order.domain.repository.OrderRepository;
import com.ddd.order.infrastructure.persistence.jpa.OrderItemJpaEntity;
import com.ddd.order.infrastructure.persistence.jpa.OrderJpaEntity;
import com.ddd.order.infrastructure.persistence.jpa.OrderJpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return jpaRepository.findById(id.value())
                .map(this::toDomain);
    }

    @Override
    public void save(Order order) {
        OrderJpaEntity entity = toJpa(order);
        if (order.getId() != null) {
            entity.setId(order.getId().value());
        }
        jpaRepository.save(entity);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private Order toDomain(OrderJpaEntity entity) {
        try {
            CustomerId customerId = new CustomerId(entity.getCustomerId());
            Address address = new Address(entity.getStreet(), entity.getCity(), entity.getZipCode());
            Order order = new Order(customerId, address);

            Field idField = AggregateRoot.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, new OrderId(entity.getId()));

            Field itemsField = Order.class.getDeclaredField("items");
            itemsField.setAccessible(true);
            List<OrderItem> items = entity.getItems().stream()
                    .map(this::toDomainItem)
                    .collect(Collectors.toList());
            itemsField.set(order, items);

            Field totalAmountField = Order.class.getDeclaredField("totalAmount");
            totalAmountField.setAccessible(true);
            totalAmountField.set(order, new Money(entity.getTotalAmount(), entity.getCurrency()));

            Field statusField = Order.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(order, OrderStatus.valueOf(entity.getStatus()));

            Field submittedField = Order.class.getDeclaredField("submitted");
            submittedField.setAccessible(true);
            submittedField.setBoolean(order, true);

            Field createdAtField = Order.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(order, entity.getCreatedAt());

            return order;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reconstruct Order from JPA entity", e);
        }
    }

    private OrderItem toDomainItem(OrderItemJpaEntity entity) {
        OrderItem item = new OrderItem(
                new ProductId(entity.getProductId()),
                entity.getProductName(),
                entity.getQuantity(),
                new Money(entity.getUnitPrice(), entity.getCurrency())
        );
        item.setOrderItemId(entity.getId());
        return item;
    }

    private OrderJpaEntity toJpa(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        if (order.getId() != null) {
            entity.setId(order.getId().value());
        }
        entity.setCustomerId(order.getCustomerId().value());
        entity.setTotalAmount(order.getTotalAmount().amount());
        entity.setCurrency(order.getTotalAmount().currency());
        entity.setStatus(order.getStatus().name());
        entity.setStreet(order.getShippingAddress().street());
        entity.setCity(order.getShippingAddress().city());
        entity.setZipCode(order.getShippingAddress().zipCode());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(java.time.Instant.now());

        List<OrderItemJpaEntity> itemEntities = order.getItems().stream()
                .map(item -> toJpaItem(item, entity))
                .toList();
        entity.setItems(itemEntities);

        return entity;
    }

    private OrderItemJpaEntity toJpaItem(OrderItem item, OrderJpaEntity orderEntity) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        if (item.getOrderItemId() != null) {
            entity.setId(item.getOrderItemId());
        }
        entity.setProductId(item.getProductId().value());
        entity.setProductName(item.getProductName());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice().amount());
        entity.setCurrency(item.getUnitPrice().currency());
        entity.setSubtotal(item.getSubtotal().amount());
        entity.setOrder(orderEntity);
        return entity;
    }
}
