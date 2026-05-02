package org.example.polify.common.error;

public record FieldErrorResponse(
    String field,
    String message
) {
}

