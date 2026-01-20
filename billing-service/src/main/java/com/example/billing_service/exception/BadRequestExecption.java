package com.example.billing_service.exception;

public class BadRequestExecption extends RuntimeException {
    public BadRequestExecption(String message) {
        super(message);
    }
}
