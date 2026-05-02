package org.example.polify.survey;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.example.polify.common.error.ApiError;
import org.example.polify.survey.dto.SurveyDetailsResponse;
import org.example.polify.survey.dto.SurveyListItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/surveys")
@Tag(name = "Surveys", description = "Survey definitions (read-only in MVP).")
public class SurveyController {
    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    @Operation(summary = "List surveys", description = "Returns list of published surveys.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SurveyListItem.class)))
    })
    public List<SurveyListItem> list() {
        return surveyService.listSurveys();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get survey details", description = "Returns survey with questions and options.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SurveyDetailsResponse.class))),
        @ApiResponse(responseCode = "404", description = "Survey not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public SurveyDetailsResponse get(@PathVariable long id) {
        return surveyService.getSurvey(id);
    }
}
