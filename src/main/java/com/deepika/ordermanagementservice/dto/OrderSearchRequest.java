package com.deepika.ordermanagementservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class OrderSearchRequest {

        @Min(0)
        private int page = 0;

        @Min(1)
        @Max(100)
        private int size = 10;

        private String sortBy = "price";

        private String sortDirection = "asc";
}
