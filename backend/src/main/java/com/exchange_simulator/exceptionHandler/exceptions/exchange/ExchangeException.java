package com.exchange_simulator.exceptionHandler.exceptions.exchange;

import com.exchange_simulator.exceptionHandler.exceptions.visible.VisibleException;

public class ExchangeException extends RuntimeException implements VisibleException {
    public ExchangeException(String message) {
        super(message);
    }
}
