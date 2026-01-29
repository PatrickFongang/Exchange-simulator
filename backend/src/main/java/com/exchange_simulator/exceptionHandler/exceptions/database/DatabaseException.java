package com.exchange_simulator.exceptionHandler.exceptions.database;

import com.exchange_simulator.exceptionHandler.exceptions.visible.VisibleException;

public class DatabaseException extends RuntimeException implements VisibleException{
    public DatabaseException(String message) {
        super(message);
    }
}
