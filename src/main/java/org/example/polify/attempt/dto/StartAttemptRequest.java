package org.example.polify.attempt.dto;

import jakarta.validation.constraints.Positive;

public class StartAttemptRequest {
    @Positive
    private long surveyId;

    public long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(long surveyId) {
        this.surveyId = surveyId;
    }
}

