package org.example.polify.attempt;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.example.polify.attempt.dto.StartAttemptRequest;
import org.example.polify.attempt.dto.StartAttemptResponse;
import org.example.polify.attempt.dto.AttemptDetailsResponse;
import org.example.polify.attempt.dto.ActiveAttemptResponse;
import org.example.polify.attempt.dto.SubmitAnswerRequest;
import org.example.polify.auth.PolifyPrincipal;
import org.example.polify.common.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attempts")
@Tag(name = "Attempts", description = "Survey execution: start attempt, submit answers, complete for reward.")
@SecurityRequirement(name = "bearerAuth")
public class AttemptController {
    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Start attempt", description = "Starts an attempt for a survey. Only one IN_PROGRESS attempt per user is allowed.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = StartAttemptResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Survey not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Attempt not allowed", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public StartAttemptResponse start(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @Valid @RequestBody StartAttemptRequest request
    ) {
        long attemptId = attemptService.startAttempt(principal.userId(), request.getSurveyId());
        return new StartAttemptResponse(attemptId);
    }

    @PostMapping("/{attemptId}/answers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Submit answer",
        description = "Creates or updates one answer in an attempt. Payload must match the question type. " +
            "Also enforces sequential required rule: cannot answer later questions while earlier required are missing."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Saved"),
        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Attempt/Question not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Attempt not allowed (completed/expired)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void submitAnswer(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @PathVariable long attemptId,
        @Valid @RequestBody SubmitAnswerRequest request
    ) {
        attemptService.submitAnswer(principal.userId(), attemptId, request);
    }

    @PostMapping("/{attemptId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Complete attempt", description = "Completes an attempt and atomically creates a reward ledger entry.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Completed"),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Attempt not found", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Attempt not allowed (completed/expired)", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "400", description = "Missing required answers", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public void complete(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @PathVariable long attemptId
    ) {
        attemptService.completeAttempt(principal.userId(), attemptId);
    }

    @GetMapping("/{attemptId}")
    @Operation(summary = "Get attempt details", description = "Returns attempt status, saved answers and nextQuestionId for resume.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AttemptDetailsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Attempt not found", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public AttemptDetailsResponse get(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @PathVariable long attemptId
    ) {
        return attemptService.getAttempt(principal.userId(), attemptId);
    }

    @GetMapping
    @Operation(summary = "List attempts", description = "Lists attempts for current user (optional filter by surveyId).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AttemptDetailsResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<AttemptDetailsResponse> list(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @RequestParam(required = false) Long surveyId
    ) {
        return attemptService.listAttempts(principal.userId(), surveyId);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active attempt for survey", description = "Returns current IN_PROGRESS attempt for survey, or null.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ActiveAttemptResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ActiveAttemptResponse> active(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @RequestParam long surveyId
    ) {
        ActiveAttemptResponse resp = attemptService.findActiveAttempt(principal.userId(), surveyId);
        if (resp == null) {
            return ResponseEntity.ok().body(null);
        }
        return ResponseEntity.ok(resp);
    }
}
