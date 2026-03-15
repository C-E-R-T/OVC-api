package com.example.ovcbackend.auth.exception;

public class AuthBadRequestException extends RuntimeException {
    public AuthBadRequestException(String message) {
        super(message);
    }
}
