package com.exchange_simulator.dto.error;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


public record ErrorResponseDto(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Optional<Map<String, String>> validationErrors
)
{}
