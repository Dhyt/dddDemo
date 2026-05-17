package com.ddd.order.application.service;

import com.ddd.order.application.assembler.OrderAssembler;
import com.ddd.order.application.dto.CreateOrderRequest;
import com.ddd.order.application.dto.OrderResponse;
import com.ddd.order.domain.model.Order;
import com.ddd.order.domain.model.OrderId;
import com.ddd.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderAssembler orderAssembler;

    public OrderApplicationService(OrderRepository orderRepository, OrderAssembler orderAssembler) {
        this.orderRepository = orderRepository;
        this.orderAssembler = orderAssembler;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = orderAssembler.toDomain(request);
        order.submit();
        orderRepository.save(order);
        return orderAssembler.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(new OrderId(id))
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + id));
        return orderAssembler.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderAssembler::toResponse)
                .toList();
    }

    public OrderResponse cancelOrder(Long id) {
        Order order = orderRepository.findById(new OrderId(id))
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + id));
        order.cancel();
        orderRepository.save(order);
        return orderAssembler.toResponse(order);
    }

    public OrderResponse payOrder(Long id) {
        Order order = orderRepository.findById(new OrderId(id))
                .orElseThrow(() -> new IllegalArgumentException("订单不存在: " + id));
        order.markPaid();
        orderRepository.save(order);
        return orderAssembler.toResponse(order);
    }
}
