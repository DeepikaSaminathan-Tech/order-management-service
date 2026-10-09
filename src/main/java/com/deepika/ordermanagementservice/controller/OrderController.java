package com.deepika.ordermanagementservice.controller;

import com.deepika.ordermanagementservice.dto.OrderSearchRequest;
import com.deepika.ordermanagementservice.dto.OrderRequest;
import com.deepika.ordermanagementservice.dto.OrderResponse;
import com.deepika.ordermanagementservice.dto.OrderUpdateRequest;
import com.deepika.ordermanagementservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }


    @GetMapping("/page")
    public ResponseEntity<Page<OrderResponse>> getOrderByPage(
            @Valid OrderSearchRequest orderSearchRequest
    ) {
        return ResponseEntity.ok(orderService.getOrderByPage(orderSearchRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrderByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrderByStatus(status));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("Idempotency-Key")  String idempotencyKey,
            @Valid @RequestBody OrderRequest orderRequest) {
        OrderResponse savedOrder = orderService.createOrder(orderRequest, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderUpdateRequest request) {

        return ResponseEntity.ok(orderService.updateOrder(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }

}
