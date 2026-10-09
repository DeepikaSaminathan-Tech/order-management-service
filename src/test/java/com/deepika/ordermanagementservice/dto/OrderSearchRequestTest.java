package com.deepika.ordermanagementservice.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderSearchRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    /**
     * Scenarios covered:
     * 1. Default pagination and sorting values.
     * 2. Custom page and size values.
     * 3. Negative page is invalid.
     * 4. Zero size is invalid.
     * 5. Size greater than 100 is invalid.
     */

    @Test
    @DisplayName("Should use default pagination and sorting values")
    void shouldUseDefaultValues() {
        OrderSearchRequest request = new OrderSearchRequest();

        assertEquals(0, request.getPage());
        assertEquals(10, request.getSize());
        assertEquals("price", request.getSortBy());
        assertEquals("asc", request.getSortDirection());
    }

    @Test
    @DisplayName("Should accept custom page and size")
    void shouldAcceptCustomPageAndSize() {
        OrderSearchRequest request = new OrderSearchRequest();
        request.setPage(2);
        request.setSize(20);

        assertEquals(2, request.getPage());
        assertEquals(20, request.getSize());
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Should reject negative page number")
    void shouldRejectNegativePage() {
        OrderSearchRequest request = new OrderSearchRequest();
        request.setPage(-1);

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Should reject zero page size")
    void shouldRejectZeroSize() {
        OrderSearchRequest request = new OrderSearchRequest();
        request.setSize(0);

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    @DisplayName("Should reject page size greater than 100")
    void shouldRejectSizeGreaterThan100() {
        OrderSearchRequest request = new OrderSearchRequest();
        request.setSize(101);

        assertFalse(validator.validate(request).isEmpty());
    }
}