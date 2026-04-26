package org.example.polify.attempt;

public class AttemptNotFoundException extends RuntimeException {
    private final long attemptId;

    public AttemptNotFoundException(long attemptId) {
        super("Attempt not found: " + attemptId);
        this.attemptId = attemptId;
    }

    public long getAttemptId() {
        return attemptId;
    }
}

