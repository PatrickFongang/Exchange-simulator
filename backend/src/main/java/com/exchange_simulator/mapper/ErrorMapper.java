package com.exchange_simulator.mapper;

import com.exchange_simulator.dto.error.ErrorResponseDto;
import com.exchange_simulator.exceptionHandler.exceptions.visible.VisibleException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class ErrorMapper {
    public ErrorResponseDto buildResponse(HttpStatus status, Exception ex, Map<String, String> validationErrors, HttpServletRequest request) {
        var message = "Internal Server Error";
        if (ex instanceof VisibleException) message = ex.getMessage();
        else if (validationErrors != null && !validationErrors.isEmpty())
            message = "Fields did not complete validation";

        return new ErrorResponseDto(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors
        );
    }

    public ErrorResponseDto buildResponse(HttpStatus status, Exception ex, HttpServletRequest request) {
        return buildResponse(status, ex, null, request);
    }
}
