package com.exchange_simulator.exceptionHandler.exceptions.auth;

import com.exchange_simulator.exceptionHandler.exceptions.visible.VisibleException;

public class AuthException extends RuntimeException implements VisibleException {
    public AuthException(String message) {
        super(message);
    }
}
