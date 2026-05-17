package com.ddd.order.domain.model;

import com.ddd.common.exception.DomainException;
import com.ddd.order.domain.event.OrderCancelledEvent;
import com.ddd.order.domain.event.OrderDeliveredEvent;
import com.ddd.order.domain.event.OrderPaidEvent;
import com.ddd.order.domain.event.OrderSubmittedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Money price100;
    private Money price200;
    private Address address;

    @BeforeEach
    void setUp() {
        price100 = new Money(new BigDecimal("100.00"), "CNY");
        price200 = new Money(new BigDecimal("200.00"), "CNY");
        address = new Address("北京路1号", "上海", "200000");
    }

    @Test
    void shouldCreateOrderWithItems() {
        Order order = new Order(new CustomerId(1L), address);
        order.addItem(new ProductId(1L), "商品A", 2, price100);
        order.addItem(new ProductId(2L), "商品B", 1, price200);

        assertEquals(2, order.getItems().size());
        assertEquals(0, new BigDecimal("400.00").compareTo(order.getTotalAmount().amount()));
    }

    @Test
    void shouldPublishEventWhenSubmitted() {
        Order order = new Order(new CustomerId(1L), address);
        order.addItem(new ProductId(1L), "商品A", 1, price100);

        OrderSubmittedEvent event = order.submit();
        assertNotNull(event);
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void shouldNotSubmitEmptyOrder() {
        Order order = new Order(new CustomerId(1L), address);
        assertThrows(DomainException.class, order::submit);
    }

    @Test
    void shouldCancelPendingOrder() {
        Order order = createPendingOrder();
        OrderCancelledEvent event = order.cancel();
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertNotNull(event);
    }

    @Test
    void shouldNotCancelPaidOrder() {
        Order order = createPendingOrder();
        order.submit();
        order.markPaid();
        assertThrows(DomainException.class, order::cancel);
    }

    @Test
    void shouldTransitionFromPendingToPaid() {
        Order order = createPendingOrder();
        order.submit();
        OrderPaidEvent event = order.markPaid();
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertNotNull(event);
    }

    @Test
    void shouldTransitionFromPaidToDelivered() {
        Order order = createPendingOrder();
        order.markPaid();
        order.markShipped();
        OrderDeliveredEvent event = order.markDelivered();
        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        assertNotNull(event);
    }

    @Test
    void shouldNotSubmitAfterModification() {
        Order order = new Order(new CustomerId(1L), address);
        order.addItem(new ProductId(1L), "商品A", 1, price100);
        order.submit();
        assertThrows(DomainException.class, () ->
                order.addItem(new ProductId(2L), "商品B", 1, price200));
    }

    private Order createPendingOrder() {
        Order order = new Order(new CustomerId(1L), address);
        order.addItem(new ProductId(1L), "商品A", 1, price100);
        order.submit();
        return order;
    }
}
