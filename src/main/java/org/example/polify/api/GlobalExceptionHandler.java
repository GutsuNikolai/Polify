package org.example.polify.api;

import org.example.polify.auth.DuplicateFieldException;
import org.example.polify.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateFieldException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiError("DUPLICATE", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiError("INVALID_CREDENTIALS", ex.getMessage()));
    }
}

