package com.deepika.ordermanagementservice.exception;

import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    /**
     * Scenarios covered:
     * 1. Missing order returns a 404 error response.
     * 2. Optimistic locking failure returns a 409 error response.
     * 3. Validation failure returns a 400 error response.
     */

    @Test
    @DisplayName("Should return 404 when order does not exist")
    void shouldReturn404WhenOrderDoesNotExist() {

        OrderNotFoundException exception =
                new OrderNotFoundException(99L);

        var response = handler.handleOrderNotFoundException(exception);

        assertEquals(404, response.status());
        assertEquals("Could not find order with id: 99", response.message());
    }

    @Test
    @DisplayName("Should return 409 when optimistic locking fails")
    void shouldReturn409WhenOptimisticLockingFails() {

        OptimisticLockException exception =
                new OptimisticLockException("Concurrent update - Order was modified by another request. Please retry.");

        var response = handler.handleOptimisticLockException(exception);

        assertEquals(409, response.status());
        assertEquals(
                "Concurrent update - Order was modified by another request. Please retry.",
                response.message()
        );
    }

    @Test
    @DisplayName("Should return 400 when request validation fails")
    void shouldReturn400WhenRequestValidationFails() {

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult()).thenReturn(
                new org.springframework.validation.BeanPropertyBindingResult(
                        new Object(), "request"
                )
        );

        var response = handler.handleMethodArgumentNotValidException(exception);

        assertEquals(400, response.status());
        assertEquals("Invalid Request", response.message());
    }
}