package com.exchange_simulator.exceptionHandler.exceptions.exchange;

import com.exchange_simulator.entity.User;

public class SpotPositionNotFoundException extends ExchangeException {
    public SpotPositionNotFoundException(User user, String token) {
        super(user.toString() + " does not have " + token + " token");
    }
}
