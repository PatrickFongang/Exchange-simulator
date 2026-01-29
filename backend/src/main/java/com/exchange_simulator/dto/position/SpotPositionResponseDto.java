package com.exchange_simulator.dto.position;

import com.exchange_simulator.entity.SpotPosition;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for {@link SpotPosition}
 */
public record SpotPositionResponseDto(
        Long positionId,
        String token,
        BigDecimal quantity,
        BigDecimal avgBuyPrice,
        BigDecimal positionValue,
        Instant timestamp
) implements Serializable
{}