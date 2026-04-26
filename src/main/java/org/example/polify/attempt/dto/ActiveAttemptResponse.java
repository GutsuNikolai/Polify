package org.example.polify.attempt.dto;

public class ActiveAttemptResponse {
    private final long attemptId;
    private final long surveyId;
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

