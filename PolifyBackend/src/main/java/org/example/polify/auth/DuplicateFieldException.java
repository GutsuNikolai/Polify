package org.example.polify.auth;

public class DuplicateFieldException extends RuntimeException {
    private final String field;

    public DuplicateFieldException(String field) {
        super("Duplicate field: " + field);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}

