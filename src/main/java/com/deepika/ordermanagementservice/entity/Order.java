package com.deepika.ordermanagementservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table (name="orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;  //Hibernate handles the concurrency check

    private String customerName;

    private String productName;

    private Integer quantity;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name="idempotency_key", unique=true, nullable=false)  //uniques constraint handles the race
    private String idempotencyKey;  //Unique identifier to prevent duplicate request/order and return cached results instead of reprocessing
}
