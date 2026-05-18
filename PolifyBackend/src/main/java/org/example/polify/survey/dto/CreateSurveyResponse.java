package org.example.polify.survey.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Create survey response.")
public record CreateSurveyResponse(
    @Schema(description = "Created survey id.", example = "10")
    long surveyId
) {
}

