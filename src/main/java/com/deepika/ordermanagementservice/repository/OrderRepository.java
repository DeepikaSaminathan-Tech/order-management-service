package com.deepika.ordermanagementservice.repository;

import com.deepika.ordermanagementservice.entity.Order;
import com.deepika.ordermanagementservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    List<Order> findByStatus(OrderStatus status);
}
