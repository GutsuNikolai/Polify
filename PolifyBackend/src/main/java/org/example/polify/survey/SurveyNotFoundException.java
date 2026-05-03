package org.example.polify.survey;

public class SurveyNotFoundException extends RuntimeException {
    private final long surveyId;

    public SurveyNotFoundException(long surveyId) {
        super("Survey not found: " + surveyId);
        this.surveyId = surveyId;
    }

    public long getSurveyId() {
        return surveyId;
    }
}

