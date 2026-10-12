package com.deepika.ordermanagementservice.dto;

import com.deepika.ordermanagementservice.entity.OrderStatus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderResponseTest {

    /**
     * Scenarios covered:
     * 1. Store order details in the response.
     * 2. Expose all six response fields correctly.
     */

    @Test
    @DisplayName("Should expose all order response fields")
    void shouldExposeAllOrderResponseFields() {
        OrderResponse response = new OrderResponse(
                1L,
                "xx",
                "xx",
                2,
                new BigDecimal("50000"),
                OrderStatus.CREATED,
                1L
        );

        assertEquals(1L, response.id());
        assertEquals("xx", response.customerName());
        assertEquals("xx", response.productName());
        assertEquals(2, response.quantity());
        assertEquals(new BigDecimal("50000"), response.price());
        assertEquals(OrderStatus.CREATED, response.status());
    }
}