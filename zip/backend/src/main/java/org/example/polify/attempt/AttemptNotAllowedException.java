package org.example.polify.attempt;

public class AttemptNotAllowedException extends RuntimeException {
    public AttemptNotAllowedException(String message) {
        super(message);
    }
}

