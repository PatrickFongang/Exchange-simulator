package com.exchange_simulator.exceptionHandler;

import com.exchange_simulator.dto.error.ErrorResponseDto;
import com.exchange_simulator.exceptionHandler.exceptions.database.DatabaseException;
import com.exchange_simulator.exceptionHandler.exceptions.database.UserAlreadyExistsException;
import com.exchange_simulator.exceptionHandler.exceptions.database.UserNotFoundException;
import com.exchange_simulator.exceptionHandler.exceptions.exchange.*;
import com.exchange_simulator.exceptionHandler.exceptions.visible.VisibleException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
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
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleOrderNotFound(ExchangeException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                buildResponse(HttpStatus.BAD_REQUEST, ex, request),
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUsernameNotFound(DatabaseException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                buildResponse(HttpStatus.NOT_FOUND, ex, request),
                HttpStatus.NOT_FOUND
        );
    }
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleUserAlreadyExists(DatabaseException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                buildResponse(HttpStatus.BAD_REQUEST, ex, request),
                HttpStatus.BAD_REQUEST
        );
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return new ResponseEntity<>(
                buildResponse(HttpStatus.FORBIDDEN, ex, request),
                HttpStatus.FORBIDDEN
        );
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var errors = new HashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
           errors.put(err.getField(), err.getDefaultMessage())
        );

        return new ResponseEntity<>(
                buildResponse(HttpStatus.BAD_REQUEST, ex, Optional.of(errors), request),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnhandledError(Exception ex, HttpServletRequest request) {
        log.warn("Received unhandled error:");
        log.warn(ex.toString());
        ex.printStackTrace(System.out);

        return new ResponseEntity<>(
                buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, request),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }



    private ErrorResponseDto buildResponse(HttpStatus status, Exception ex, Optional<Map<String,String>> validationErrors, HttpServletRequest request) {
        var message = "Internal Server Error";
        if(ex instanceof VisibleException) message = ex.getMessage();
        else if(validationErrors.isPresent()) message = "Fields did not complete validation";

        return new ErrorResponseDto(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors
        );
    }

    private ErrorResponseDto buildResponse(HttpStatus status, Exception ex, HttpServletRequest request) {
        return buildResponse(status, ex, Optional.empty(), request);
    }
}