package com.exchange_simulator.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequestDto {
    private Long userId;
    private String token;
    private BigDecimal limit;
    private BigDecimal quantity;

}
