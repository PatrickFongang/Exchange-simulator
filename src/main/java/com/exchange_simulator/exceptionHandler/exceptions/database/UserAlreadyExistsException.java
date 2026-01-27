package com.exchange_simulator.exceptionHandler.exceptions.database;


public class UserAlreadyExistsException extends DatabaseException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
