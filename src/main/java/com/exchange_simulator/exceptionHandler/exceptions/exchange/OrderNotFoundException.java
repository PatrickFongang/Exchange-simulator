package com.exchange_simulator.exceptionHandler.exceptions.exchange;

public class OrderNotFoundException extends ExchangeException {
    public OrderNotFoundException(Long orderId) {
        super("Order with id " + orderId + " not found");
    }
}
