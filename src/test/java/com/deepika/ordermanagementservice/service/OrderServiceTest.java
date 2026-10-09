package com.deepika.ordermanagementservice.service;

import com.deepika.ordermanagementservice.dto.OrderRequest;
import com.deepika.ordermanagementservice.dto.OrderSearchRequest;
import com.deepika.ordermanagementservice.dto.OrderUpdateRequest;
import com.deepika.ordermanagementservice.entity.Order;
import com.deepika.ordermanagementservice.entity.OrderStatus;
import com.deepika.ordermanagementservice.exception.OrderNotFoundException;
import com.deepika.ordermanagementservice.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderService unit tests.
 * Scenarios covered:
 * 1. Retrieve an existing order.
 * 2. Handle a non-existent order.
 * 3. Create an order using a new idempotency key.
 * 4. Return an existing order when the key is reused.
 * 5. Recover when a duplicate-key database constraint is violated. etc.
 * Uses Mockito; no running database or Redis instance is required.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository);
    }

    @Test
    @DisplayName("Should return order when order exists")
    void shouldReturnOrderWhenOrderExists() {

        Order order = buildOrder(1L, "key-123");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        var response = orderService.getOrderById(1L);

        assertEquals(1L, response.id());
        assertEquals("xx", response.customerName());
        assertEquals("xx", response.productName());
        assertEquals(1, response.quantity());
        assertEquals(new BigDecimal("50000"), response.price());
        assertEquals(OrderStatus.CREATED, response.status());

        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when order does not exist")
    void shouldThrowExceptionWhenOrderDoesNotExist() {

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(99L)
        );

        verify(orderRepository).findById(99L);
    }

    @Test
    @DisplayName("Should create order when idempotency key is new")
    void shouldCreateOrderWhenIdempotencyKeyIsNew() {
        OrderRequest request = new OrderRequest(
                "xx", "xx", 1, new BigDecimal("50000")
        );

        when(orderRepository.findByIdempotencyKey("key-123"))
                .thenReturn(Optional.empty());

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return order;
                });

        var response = orderService.createOrder(request, "key-123");

        assertEquals(1L, response.id());
        assertEquals("xx", response.customerName());
        assertEquals(OrderStatus.CREATED, response.status());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should return existing order when idempotency key is reused")
    void shouldReturnExistingOrderWhenIdempotencyKeyIsReused() {
        Order existingOrder = buildOrder(1L, "key-123");

        OrderRequest request = new OrderRequest(
                "xx", "xx", 1, new BigDecimal("50000")
        );

        when(orderRepository.findByIdempotencyKey("key-123"))
                .thenReturn(Optional.of(existingOrder));

        var response = orderService.createOrder(request, "key-123");

        assertEquals(1L, response.id());
        assertEquals("xx", response.customerName());

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Should return existing order after duplicate-key insert failure")
    void shouldReturnExistingOrderAfterDuplicateKeyFailure() {
        Order existingOrder = buildOrder(1L, "key-123");

        OrderRequest request = new OrderRequest(
                "xx", "xx", 1, new BigDecimal("50000")
        );

        when(orderRepository.findByIdempotencyKey("key-123"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingOrder));

        when(orderRepository.save(any(Order.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        var response = orderService.createOrder(request, "key-123");

        assertEquals(1L, response.id());
        assertEquals("xx", response.customerName());

        verify(orderRepository).save(any(Order.class));
        verify(orderRepository, times(2))
                .findByIdempotencyKey("key-123");
    }

    @Test
    @DisplayName("Should update an existing order")
    void shouldUpdateExistingOrder() {
        Order order = buildOrder(1L, "key-123");

        OrderUpdateRequest request = new OrderUpdateRequest(
                "xx", "xx", 2, new BigDecimal("60000")
        );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        var response = orderService.updateOrder(1L, request);

        assertEquals(1L, response.id());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("60000"), response.price());

        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when updating a missing order")
    void shouldThrowExceptionWhenUpdatingMissingOrder() {
        OrderUpdateRequest request = new OrderUpdateRequest(
                "xx", "xx", 2, new BigDecimal("60000")
        );

        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrder(99L, request)
        );
    }

    @Test
    @DisplayName("Should delete an existing order")
    void shouldDeleteExistingOrder() {
        Order order = buildOrder(1L, "key-123");

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        orderService.deleteOrder(1L);

        verify(orderRepository).delete(order);
    }

    @Test
    @DisplayName("Should throw exception when deleting a missing order")
    void shouldThrowExceptionWhenDeletingMissingOrder() {
        when(orderRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.deleteOrder(99L)
        );

        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    @DisplayName("Should return orders matching the requested status")
    void shouldReturnOrdersByStatus() {
        Order createdOrder = buildOrder(1L, "key-123");

        when(orderRepository.findByStatus(OrderStatus.CREATED))
                .thenReturn(List.of(createdOrder));

        var result = orderService.getOrderByStatus(OrderStatus.CREATED);

        assertEquals(1, result.size());
        assertEquals(OrderStatus.CREATED, result.getFirst().status());

        verify(orderRepository).findByStatus(OrderStatus.CREATED);
        verify(orderRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no orders match status")
    void shouldReturnEmptyListWhenNoOrdersMatchStatus() {
        when(orderRepository.findByStatus(OrderStatus.SHIPPED))
                .thenReturn(List.of());

        var result = orderService.getOrderByStatus(OrderStatus.SHIPPED);

        assertTrue(result.isEmpty());

        verify(orderRepository).findByStatus(OrderStatus.SHIPPED);
        verify(orderRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should return paginated orders")
    void shouldReturnPaginatedOrders() {
        Order order = buildOrder(1L, "key-123");

        OrderSearchRequest request = new OrderSearchRequest();
        request.setPage(0);
        request.setSize(10);
        request.setSortBy("id");
        request.setSortDirection("asc");

        Pageable pageable = PageRequest.of(
                0, 10, Sort.by(Sort.Direction.ASC, "id")
        );

        Page<Order> orderPage = new PageImpl<>(
                List.of(order), pageable, 1
        );

        when(orderRepository.findAll(any(Pageable.class)))
                .thenReturn(orderPage);

        var result = orderService.getOrderByPage(request);

        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getNumber());
    }

    @Test
    @DisplayName("Should propagate database exception when duplicate order cannot be recovered")
    void shouldPropagateExceptionWhenDuplicateOrderCannotBeRecovered() {
        OrderRequest request = new OrderRequest(
                "xx", "xx", 1, new BigDecimal("50000")
        );

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("Duplicate key");

        when(orderRepository.findByIdempotencyKey("key-123"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        when(orderRepository.save(any(Order.class)))
                .thenThrow(exception);

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> orderService.createOrder(request, "key-123")
        );

        assertEquals(exception, thrown);

        verify(orderRepository, times(2))
                .findByIdempotencyKey("key-123");
    }

    private Order buildOrder(Long id, String idempotencyKey) {
        return Order.builder()
                .id(id)
                .customerName("xx")
                .productName("xx")
                .quantity(1)
                .price(new BigDecimal("50000"))
                .status(OrderStatus.CREATED)
                .idempotencyKey(idempotencyKey)
                .build();
    }
}
