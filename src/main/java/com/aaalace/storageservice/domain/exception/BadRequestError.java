package com.aaalace.storageservice.domain.exception;

public class BadRequestError extends RuntimeException {
    public BadRequestError(String message) {
        super(message);
    }
}
