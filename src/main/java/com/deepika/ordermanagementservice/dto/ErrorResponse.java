package com.deepika.ordermanagementservice.dto;

public record ErrorResponse(
        int status,
        String message
) {
}
