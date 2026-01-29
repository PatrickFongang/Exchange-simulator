package com.exchange_simulator.dto.user;

import com.exchange_simulator.entity.User;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for {@link User}
 */
public record UserResponseDto(
        Long id,
        Instant updatedAt,
        Instant createdAt,
        String username,
        String email,
        BigDecimal funds) implements Serializable
{}