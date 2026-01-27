package com.exchange_simulator.exceptionHandler.exceptions.exchange;

import java.math.BigDecimal;

public class InsufficientFundsException extends ExchangeException {
    public InsufficientFundsException(BigDecimal funds, BigDecimal required) {

        super(String.format("Insufficient funds. You have: %s, required: %s", funds, required));
    }
}
