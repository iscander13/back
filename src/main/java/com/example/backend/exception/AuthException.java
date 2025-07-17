package com.example.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED) // Это КЛЮЧЕВАЯ аннотация для статуса 401
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
