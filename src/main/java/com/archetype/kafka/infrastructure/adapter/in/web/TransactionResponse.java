package com.archetype.kafka.infrastructure.adapter.in.web;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private String id;
    private String accountId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
}
