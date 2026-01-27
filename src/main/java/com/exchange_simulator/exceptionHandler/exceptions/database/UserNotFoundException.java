package com.exchange_simulator.exceptionHandler.exceptions.database;

public class UserNotFoundException extends DatabaseException {
    public UserNotFoundException(Long userId) {
        super("User with id: " + userId + " not found");
    }
}
