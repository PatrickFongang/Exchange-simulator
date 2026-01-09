package com.exchange_simulator.dto.marketOrder;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarketOrderRequestDto {
    private Long userId;
    private String token;
    private BigDecimal quantity;
}
