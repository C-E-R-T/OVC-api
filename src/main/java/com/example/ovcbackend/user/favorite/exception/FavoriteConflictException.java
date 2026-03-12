package com.example.ovcbackend.user.favorite.exception;

public class FavoriteConflictException extends RuntimeException {
    public FavoriteConflictException(String message) {
        super(message);
    }
}
