package com.deepika.ordermanagementservice.controller;

import com.deepika.ordermanagementservice.dto.OrderRequest;
import com.deepika.ordermanagementservice.dto.OrderResponse;
import com.deepika.ordermanagementservice.dto.OrderSearchRequest;
import com.deepika.ordermanagementservice.dto.OrderUpdateRequest;
import com.deepika.ordermanagementservice.entity.OrderStatus;
import com.deepika.ordermanagementservice.exception.OrderNotFoundException;
import com.deepika.ordermanagementservice.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    /**
     * Scenarios covered:
     * 1. Create an order and return HTTP 201.
     * 2. Retrieve an order and return HTTP 200.
     * 3. Retrieve all orders and return HTTP 200.
     * 4. Delete an order and return HTTP 204.
     * The service is mocked, so no database or Redis is required.
     */

    @Test
    @DisplayName("Should create order and return 201 Created")
    void shouldCreateOrder() throws Exception {
        OrderRequest request = new OrderRequest(
                "xx", "xx", 1, new BigDecimal("50000")
        );

        OrderResponse response = new OrderResponse(
                1L, "xx", "xx", 1,
                new BigDecimal("50000"), OrderStatus.CREATED
        );

        when(orderService.createOrder(any(OrderRequest.class), eq("key-123")))
                .thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .header("Idempotency-Key", "key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("xx"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("Should retrieve order and return 200 OK")
    void shouldGetOrderById() throws Exception {
        OrderResponse response = new OrderResponse(
                1L, "xx", "xx", 1,
                new BigDecimal("50000"), OrderStatus.CREATED
        );

        when(orderService.getOrderById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("xx"));
    }

    @Test
    @DisplayName("Should retrieve all orders and return 200 OK")
    void shouldGetAllOrders() throws Exception {
        OrderResponse response = new OrderResponse(
                1L, "xx", "xx", 1,
                new BigDecimal("50000"), OrderStatus.CREATED
        );

        when(orderService.getAllOrders()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    @Test
    @DisplayName("Should delete order and return 204 No Content")
    void shouldDeleteOrder() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(1L);
    }

    @Test
    @DisplayName("Should reject order with blank customer name")
    void shouldRejectOrderWithBlankCustomerName() throws Exception {
        OrderRequest request = new OrderRequest(
                " ",
                "xx",
                1,
                new BigDecimal("50000")
        );

        mockMvc.perform(post("/api/orders")
                        .header("Idempotency-Key", "key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("Should update order and return 200 OK")
    void shouldUpdateOrder() throws Exception {
        OrderUpdateRequest request = new OrderUpdateRequest(
                "xx",
                "xx",
                2,
                new BigDecimal("60000")
        );

        OrderResponse response = new OrderResponse(
                1L,
                "xx",
                "xx",
                2,
                new BigDecimal("60000"),
                OrderStatus.CREATED
        );

        when(orderService.updateOrder(eq(1L), any(OrderUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.price").value(60000));
    }

    @Test
    @DisplayName("Should return paginated orders")
    void shouldReturnPaginatedOrders() throws Exception {
        OrderResponse response = new OrderResponse(
                1L, "xx", "xx", 1,
                new BigDecimal("50000"), OrderStatus.CREATED
        );

        Page<OrderResponse> page = new PageImpl<>(
                List.of(response),
                PageRequest.of(0, 10, Sort.by("price").ascending()),
                1
        );

        when(orderService.getOrderByPage(any(OrderSearchRequest.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/orders/page")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Should return orders filtered by status")
    void shouldGetOrdersByStatus() throws Exception {
        OrderResponse response = new OrderResponse(
                1L, "xx", "xx", 1,
                new BigDecimal("50000"), OrderStatus.CREATED
        );

        when(orderService.getOrderByStatus(OrderStatus.CREATED))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/orders/status/CREATED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    @Test
    @DisplayName("Should return empty list when no orders match status")
    void shouldReturnEmptyListForStatus() throws Exception {
        when(orderService.getOrderByStatus(OrderStatus.SHIPPED))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/orders/status/SHIPPED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should reject update with blank customer name")
    void shouldRejectInvalidUpdate() throws Exception {
        OrderUpdateRequest request = new OrderUpdateRequest(
                " ", "xx", 1, new BigDecimal("50000")
        );

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(orderService, never())
                .updateOrder(eq(1L), any(OrderUpdateRequest.class));
    }

    @Test
    @DisplayName("Should reject create request without idempotency key")
    void shouldRejectCreateWithoutIdempotencyKey() throws Exception {
        OrderRequest request = new OrderRequest(
                "xx", "xx", 1, new BigDecimal("50000")
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("Should return 404 when requested order does not exist")
    void shouldReturn404ForMissingOrder() throws Exception {
        when(orderService.getOrderById(99L))
                .thenThrow(new OrderNotFoundException(99L));

        mockMvc.perform(get("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}