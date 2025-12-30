package com.exchange_simulator.dto.user;

import lombok.Value;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link com.exchange_simulator.entity.User}
 */
@Value
public class UserResponseDto implements Serializable {
    Long id;
    Instant updatedAt;
    Instant createdAt;
    String name;
    String email;
}