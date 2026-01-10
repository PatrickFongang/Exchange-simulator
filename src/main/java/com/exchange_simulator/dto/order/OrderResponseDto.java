package com.exchange_simulator.dto.order;


import com.exchange_simulator.enums.OrderType;
import com.exchange_simulator.enums.TransactionType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponseDto(
        Long userId,
        Long orderId,
        Instant createdAt,
        String token,
        BigDecimal quantity,
        BigDecimal entry,
        BigDecimal orderValue,
        TransactionType transactionType,
        OrderType orderType,
        Instant closedAt) implements Serializable
{}
