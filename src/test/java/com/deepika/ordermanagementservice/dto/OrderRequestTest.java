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
 * Unit tests for OrderRequest Bean Validation.
 * Scenarios covered:
 * 1. Reject blank customer and product names.
 * 2. Reject null, zero, and negative quantities.
 * 3. Reject null, zero, and negative prices.
 * 4. Accept a valid order request.
 */
class OrderRequestTest {

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
        OrderRequest request = new OrderRequest(
                " ",
                "xx",
                1,
                new BigDecimal("100")
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("customerName")));
    }

    @Test
    @DisplayName("Should reject blank product name")
    void shouldRejectBlankProductName() {
        OrderRequest request = new OrderRequest(
                "xx",
                "",
                1,
                new BigDecimal("100")
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("productName")));
    }

    @Test
    @DisplayName("Should reject null quantity")
    void shouldRejectNullQuantity() {
        OrderRequest request = new OrderRequest(
                "xx",
                "xx",
                null,
                new BigDecimal("100")
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("quantity")));
    }

    @Test
    @DisplayName("Should reject zero quantity")
    void shouldRejectZeroQuantity() {
        OrderRequest request = new OrderRequest(
                "xx",
                "xx",
                0,
                new BigDecimal("100")
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("quantity")));
    }

    @Test
    @DisplayName("Should reject negative quantity")
    void shouldRejectNegativeQuantity() {
        OrderRequest request = new OrderRequest(
                "xx",
                "xx",
                -1,
                new BigDecimal("100")
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("quantity")));
    }

    @Test
    @DisplayName("Should reject null price")
    void shouldRejectNullPrice() {
        OrderRequest request = new OrderRequest(
                "xx",
                "xx",
                1,
                null
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("price")));
    }

    @Test
    @DisplayName("Should reject zero price")
    void shouldRejectZeroPrice() {
        OrderRequest request = new OrderRequest(
                "xx",
                "xx",
                1,
                BigDecimal.ZERO
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("price")));
    }

    @Test
    @DisplayName("Should reject negative price")
    void shouldRejectNegativePrice() {
        OrderRequest request = new OrderRequest(
                "xx",
                "xx",
                1,
                new BigDecimal("-10")
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(v -> v.getPropertyPath().toString()
                        .equals("price")));
    }

    @Test
    @DisplayName("Should accept valid order request")
    void shouldAcceptValidOrderRequest() {
        OrderRequest request = new OrderRequest(
                "xx",
                "xx",
                1,
                new BigDecimal("100")
        );

        assertTrue(validator.validate(request).isEmpty());
    }
}