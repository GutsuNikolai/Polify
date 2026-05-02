package org.example.polify.attempt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Start attempt request.")
public class StartAttemptRequest {
    @Positive
    @Schema(description = "Survey id to start.", example = "1")
    private long surveyId;

    public long getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(long surveyId) {
        this.surveyId = surveyId;
    }
}
