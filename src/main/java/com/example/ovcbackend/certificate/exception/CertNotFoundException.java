package com.example.ovcbackend.certificate.exception;

public class CertNotFoundException extends RuntimeException {
    public CertNotFoundException(String message) {
        super(message);
    }
}
