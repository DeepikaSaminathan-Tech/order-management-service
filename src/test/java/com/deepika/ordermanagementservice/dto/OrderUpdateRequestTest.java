package com.deepika.ordermanagementservice.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for OrderUpdateRequest Bean Validation.
 * Scenarios covered:
 * 1. Reject blank customer and product names.
 * 2. Reject null or non-positive quantity.
 * 3. Reject null or non-positive price.
 * 4. Accept a valid update request.
 */
class OrderUpdateRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    @DisplayName("Should reject blank customer name")
    void shouldRejectBlankCustomerName() {
        var request = new OrderUpdateRequest(
                " ",
                "xx",
                1,
                new BigDecimal("100"),
                1L
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("customerName")));
    }

    @Test
    @DisplayName("Should reject blank product name")
    void shouldRejectBlankProductName() {
        var request = new OrderUpdateRequest(
                "xx",
                "",
                1,
                new BigDecimal("100"),
                1L
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("productName")));
    }

    @Test
    @DisplayName("Should reject null quantity")
    void shouldRejectNullQuantity() {
        var request = new OrderUpdateRequest(
                "xx",
                "xx",
                null,
                new BigDecimal("100"),
                1L
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("quantity")));
    }

    @Test
    @DisplayName("Should reject zero quantity")
    void shouldRejectZeroQuantity() {
        var request = new OrderUpdateRequest(
                "xx",
                "xx",
                0,
                new BigDecimal("100"),
                1L
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("quantity")));
    }

    @Test
    @DisplayName("Should reject null price")
    void shouldRejectNullPrice() {
        var request = new OrderUpdateRequest(
                "xx",
                "xx",
                1,
                null,
                1L
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("price")));
    }

    @Test
    @DisplayName("Should reject zero price")
    void shouldRejectZeroPrice() {
        var request = new OrderUpdateRequest(
                "xx",
                "xx",
                1,
                BigDecimal.ZERO,
                1L
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("price")));
    }

    @Test
    @DisplayName("Should accept valid update request")
    void shouldAcceptValidUpdateRequest() {
        var request = new OrderUpdateRequest(
                "xx",
                "xx",
                1,
                new BigDecimal("100"),
                1L
        );

        assertTrue(validator.validate(request).isEmpty());
    }
}