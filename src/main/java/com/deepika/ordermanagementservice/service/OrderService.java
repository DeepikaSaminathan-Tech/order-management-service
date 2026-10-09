package com.deepika.ordermanagementservice.service;

import com.deepika.ordermanagementservice.dto.OrderSearchRequest;
import com.deepika.ordermanagementservice.dto.OrderRequest;
import com.deepika.ordermanagementservice.dto.OrderResponse;
import com.deepika.ordermanagementservice.dto.OrderUpdateRequest;
import com.deepika.ordermanagementservice.entity.Order;
import com.deepika.ordermanagementservice.entity.OrderStatus;
import com.deepika.ordermanagementservice.exception.OrderNotFoundException;
import com.deepika.ordermanagementservice.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse createOrder(OrderRequest request, String idempotencyKey) {

        //findByIdempotencyKey - Handles application retries
        return orderRepository.findByIdempotencyKey(idempotencyKey)
                .map(this::toResponse)
                .orElseGet(
                        () -> createNewOrder(request, idempotencyKey));
    }

    private OrderResponse createNewOrder(
            OrderRequest request,
            String idempotencyKey) {

        try {

            log.info("Creating order for customer={}, product={}",
                    request.customerName(),
                    request.productName());

            Order order = Order.builder()
                    .customerName(request.customerName())
                    .productName(request.productName())
                    .quantity(request.quantity())
                    .price(request.price())
                    .status(OrderStatus.CREATED)
                    .idempotencyKey(idempotencyKey)
                    .build();

            Order savedOrder = orderRepository.save(order);

            log.info("Order created successfully, orderId={}", savedOrder.getId());

            return toResponse(savedOrder);

        } catch (DataIntegrityViolationException exception) {

            log.info("Duplicate idempotency key detected");

            // Another request created with the same idempotency key
            return orderRepository.findByIdempotencyKey(idempotencyKey)
                    .map(this::toResponse)
                    .orElseThrow(() -> exception);
        }
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(value="orders", key="#id")
    public OrderResponse getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(id));

        return toResponse(order);
    }

    public List<OrderResponse> getOrderByStatus(String status) {
        return orderRepository.findAll()
                .stream()
                .filter(order -> order.getStatus().name().equals(status))
                .map(this::toResponse)
                .toList();
    }

    public Page<OrderResponse> getOrderByPage(OrderSearchRequest orderSearchRequest) {
        Pageable pageable = PageRequest.of(orderSearchRequest.getPage(), orderSearchRequest.getSize(),
                Sort.by(Sort.Direction.fromString(orderSearchRequest.getSortDirection()),
                        orderSearchRequest.getSortBy()));

        return orderRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional
    @CacheEvict(value="orders", key="#id")
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        order.setCustomerName(request.customerName());
        order.setProductName(request.productName());
        order.setQuantity(request.quantity());
        order.setPrice(request.price());

        /* Once the entity loaded inside transaction, it is managed entity. With @Transactional, Hibernate detects changed field and persist them during commit
        Order updatedOrder = orderRepository.save(order);
        return toResponse(updatedOrder);*/

        return toResponse(order);
    }

    @CacheEvict(value="orders", key="#id")
    public void deleteOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        orderRepository.delete(order);
    }

    private OrderResponse toResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getStatus()
        );
    }

}
