package org.example.polify.survey;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.example.polify.auth.PolifyPrincipal;
import org.example.polify.common.error.ApiError;
import org.example.polify.survey.dto.CreateSurveyRequest;
import org.example.polify.survey.dto.CreateSurveyResponse;
import org.example.polify.survey.dto.SurveyDetailsResponse;
import org.example.polify.survey.dto.SurveyListItem;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/surveys")
@Tag(name = "Surveys", description = "Survey definitions. Creation is moderator-only in MVP.")
public class SurveyController {
    private final SurveyService surveyService;
    private final SurveyAdminService adminService;

    public SurveyController(SurveyService surveyService, SurveyAdminService adminService) {
        this.surveyService = surveyService;
        this.adminService = adminService;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create survey (moderator only)", description = "Creates a new survey with questions and options. Immutable after creation in MVP.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = CreateSurveyResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CreateSurveyResponse create(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @Valid @RequestBody CreateSurveyRequest request
    ) {
        long id = adminService.createSurvey(principal.userId(), request);
        return new CreateSurveyResponse(id);
    }
}
