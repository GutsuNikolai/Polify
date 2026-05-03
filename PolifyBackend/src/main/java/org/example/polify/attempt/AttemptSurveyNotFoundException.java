package org.example.polify.attempt;

public class AttemptSurveyNotFoundException extends RuntimeException {
    private final long surveyId;

    public AttemptSurveyNotFoundException(long surveyId) {
        super("Survey not found: " + surveyId);
        this.surveyId = surveyId;
    }

    public long getSurveyId() {
        return surveyId;
    }
}

