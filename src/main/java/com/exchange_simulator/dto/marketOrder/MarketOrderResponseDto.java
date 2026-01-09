package com.exchange_simulator.dto.marketOrder;


import com.exchange_simulator.enums.OrderType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

public record MarketOrderResponseDto(
        Long userId,
        Long orderId,
        Instant createdAt,
        String token,
        BigDecimal quantity,
        BigDecimal tokenPrice,
        BigDecimal orderValue,
        OrderType orderType) implements Serializable
{}
