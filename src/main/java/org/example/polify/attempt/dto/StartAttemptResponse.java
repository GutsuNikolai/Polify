package org.example.polify.attempt.dto;

public class StartAttemptResponse {
    private final long attemptId;

    public StartAttemptResponse(long attemptId) {
        this.attemptId = attemptId;
    }

    public long getAttemptId() {
        return attemptId;
    }
}

