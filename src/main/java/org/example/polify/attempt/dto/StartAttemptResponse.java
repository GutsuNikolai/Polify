package org.example.polify.attempt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Start attempt response.")
public class StartAttemptResponse {
    @Schema(description = "Created attempt id.", example = "5")
    private final long attemptId;

    public StartAttemptResponse(long attemptId) {
        this.attemptId = attemptId;
    }

    public long getAttemptId() {
        return attemptId;
    }
}
