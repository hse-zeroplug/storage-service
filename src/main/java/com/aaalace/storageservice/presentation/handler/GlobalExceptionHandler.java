package com.aaalace.storageservice.presentation.handler;

import com.aaalace.storageservice.domain.exception.BadRequestError;
import com.aaalace.storageservice.domain.exception.InternalServerError;
import com.aaalace.storageservice.domain.generic.GenericJsonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestError.class)
    public ResponseEntity<GenericJsonResponse<Object>> handleRuntimeException(Exception ex) {
        return ResponseEntity
                .badRequest()
                .body(GenericJsonResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(InternalServerError.class)
    public ResponseEntity<GenericJsonResponse<Object>> handleInternalException(Exception ex) {
        return ResponseEntity
                .internalServerError()
                .body(GenericJsonResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericJsonResponse<Object>> handleGenericException(Exception ex) {
        return ResponseEntity
                .internalServerError()
                .body(GenericJsonResponse.failure("Internal server error"));
    }
}