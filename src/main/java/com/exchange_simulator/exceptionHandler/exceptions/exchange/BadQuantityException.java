package com.exchange_simulator.exceptionHandler.exceptions.exchange;

import java.math.BigDecimal;

public class BadQuantityException extends ExchangeException {
    public BadQuantityException(BigDecimal quantity) {
        super(quantity + " is not a valid quantity number");
    }
}
