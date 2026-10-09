package com.deepika.ordermanagementservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        org.slf4j.MDC.clear();
    }

    /**
     * Scenarios covered:
     * 1. Preserve an incoming correlation ID.
     * 2. Generate an ID when the header is missing.
     * 3. Add the ID to the response header.
     * 4. Clear the MDC after request processing.
     */

    @Test
    @DisplayName("Should preserve incoming correlation ID")
    void shouldPreserveIncomingCorrelationId()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Correlation-Id", "test-correlation-123");

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(
                "test-correlation-123",
                response.getHeader("X-Correlation-Id")
        );

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should generate correlation ID when header is missing")
    void shouldGenerateCorrelationIdWhenMissing()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String correlationId = response.getHeader("X-Correlation-Id");

        assertNotNull(correlationId);
        assertFalse(correlationId.isBlank());
    }

    @Test
    @DisplayName("Should clear MDC after request processing")
    void shouldClearMdcAfterRequest()
            throws ServletException, IOException {

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("X-Correlation-Id", "test-correlation-123");

        FilterChain chain = (req, res) ->
                assertEquals(
                        "test-correlation-123",
                        org.slf4j.MDC.get("X-Correlation-Id")
                );

        filter.doFilter(request, response, chain);

        assertNull(org.slf4j.MDC.get("X-Correlation-Id"));
    }
}