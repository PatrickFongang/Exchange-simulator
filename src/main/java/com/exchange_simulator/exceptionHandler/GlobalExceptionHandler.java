package com.exchange_simulator.exceptionHandler;

import com.exchange_simulator.dto.error.ErrorResponseDto;
import com.exchange_simulator.exceptionHandler.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponseDto> handleInsufficientFunds(InsufficientFundsException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                buildResponse(HttpStatus.BAD_REQUEST, ex, request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                buildResponse(HttpStatus.NOT_FOUND, ex, request),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(ExchangeException.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralExchange(ExchangeException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                buildResponse(HttpStatus.BAD_REQUEST, ex, request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(SpotPositionNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleSpotPositionNotFound(ExchangeException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                buildResponse(HttpStatus.BAD_REQUEST, ex, request),
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(NotEnoughResourcesException.class)
    public ResponseEntity<ErrorResponseDto> handleNotEnoughResources(ExchangeException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                buildResponse(HttpStatus.BAD_REQUEST, ex, request),
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(BadQuantityException.class)
    public ResponseEntity<ErrorResponseDto> handleBadQuantity(ExchangeException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                buildResponse(HttpStatus.BAD_REQUEST, ex, request),
                HttpStatus.BAD_REQUEST
        );
    }

    private ErrorResponseDto buildResponse(HttpStatus status, Exception ex, HttpServletRequest request) {
        return new ErrorResponseDto(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
    }
}