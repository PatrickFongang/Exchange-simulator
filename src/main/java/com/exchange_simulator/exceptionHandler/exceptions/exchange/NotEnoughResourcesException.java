package com.exchange_simulator.exceptionHandler.exceptions.exchange;

import java.math.BigDecimal;

public class NotEnoughResourcesException extends ExchangeException {
    public NotEnoughResourcesException(BigDecimal order, BigDecimal owned) {

        super("Not enough resources. You have: " + owned + ", and wanted to sell: " + order);
    }
}
