package com.deepika.ordermanagementservice.dto;

import com.deepika.ordermanagementservice.entity.OrderStatus;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        Long version,
        String customerName,
        String productName,
        Integer quantity,
        BigDecimal price,
        OrderStatus status
) {
}
