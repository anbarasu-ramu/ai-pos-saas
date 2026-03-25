package com.anbu.aipos.application.exception;

import org.springframework.http.HttpStatus;

public class RegistrationException extends RuntimeException {

    private final HttpStatus status;

    public RegistrationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public RegistrationException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
