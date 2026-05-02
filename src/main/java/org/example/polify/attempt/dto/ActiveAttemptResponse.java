package org.example.polify.attempt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Active attempt info for a survey (IN_PROGRESS).")
public class ActiveAttemptResponse {
    @Schema(description = "Attempt id.", example = "5")
    private final long attemptId;
    @Schema(description = "Survey id.", example = "1")
    private final long surveyId;
    @Schema(description = "Attempt status.", example = "IN_PROGRESS")
    private final String status;

    public ActiveAttemptResponse(long attemptId, long surveyId, String status) {
        this.attemptId = attemptId;
        this.surveyId = surveyId;
        this.status = status;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public long getSurveyId() {
        return surveyId;
    }

    public String getStatus() {
        return status;
    }
}
