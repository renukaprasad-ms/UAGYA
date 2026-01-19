package com.example.user_service.exception;

public class InternalServerException extends RuntimeException {
    public InternalServerException (String message) {
        super(message);
    }
}
