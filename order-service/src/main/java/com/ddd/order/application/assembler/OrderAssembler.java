package com.ddd.order.application.assembler;

import com.ddd.order.application.dto.CreateOrderRequest;
import com.ddd.order.application.dto.CreateOrderRequest.OrderItemRequest;
import com.ddd.order.application.dto.OrderResponse;
import com.ddd.order.application.dto.OrderResponse.OrderItemResponse;
import com.ddd.order.domain.model.*;
import org.springframework.stereotype.Component;

@Component
public class OrderAssembler {

    public Order toDomain(CreateOrderRequest request) {
        CustomerId customerId = new CustomerId(request.customerId());
        Address address = new Address(request.street(), request.city(), request.zipCode());
        Order order = new Order(customerId, address);

        for (OrderItemRequest item : request.items()) {
            order.addItem(
                    new ProductId(item.productId()),
                    item.productName(),
                    item.quantity(),
                    new Money(item.unitPrice(), "CNY")
            );
        }

        return order;
    }

    public OrderResponse toResponse(Order order) {
        OrderId orderId = order.getId();
        return new OrderResponse(
                orderId != null ? orderId.value() : null,
                order.getCustomerId().value(),
                order.getTotalAmount().amount(),
                order.getTotalAmount().currency(),
                order.getStatus().name(),
                order.getItems().stream()
                        .map(this::toItemResponse)
                        .toList(),
                order.getCreatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId().value(),
                item.getProductName(),
                item.getQuantity(),
                item.getUnitPrice().amount(),
                item.getSubtotal().amount()
        );
    }
}
