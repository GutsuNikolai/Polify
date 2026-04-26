package org.example.polify.attempt;

import jakarta.validation.Valid;
import java.util.List;
import org.example.polify.attempt.dto.StartAttemptRequest;
import org.example.polify.attempt.dto.StartAttemptResponse;
import org.example.polify.attempt.dto.AttemptDetailsResponse;
import org.example.polify.attempt.dto.ActiveAttemptResponse;
import org.example.polify.attempt.dto.SubmitAnswerRequest;
import org.example.polify.auth.PolifyPrincipal;
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
public class AttemptController {
    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public StartAttemptResponse start(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @Valid @RequestBody StartAttemptRequest request
    ) {
        long attemptId = attemptService.startAttempt(principal.userId(), request.getSurveyId());
        return new StartAttemptResponse(attemptId);
    }

    @PostMapping("/{attemptId}/answers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submitAnswer(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @PathVariable long attemptId,
        @Valid @RequestBody SubmitAnswerRequest request
    ) {
        attemptService.submitAnswer(principal.userId(), attemptId, request);
    }

    @PostMapping("/{attemptId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void complete(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @PathVariable long attemptId
    ) {
        attemptService.completeAttempt(principal.userId(), attemptId);
    }

    @GetMapping("/{attemptId}")
    public AttemptDetailsResponse get(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @PathVariable long attemptId
    ) {
        return attemptService.getAttempt(principal.userId(), attemptId);
    }

    @GetMapping
    public List<AttemptDetailsResponse> list(
        @AuthenticationPrincipal PolifyPrincipal principal,
        @RequestParam(required = false) Long surveyId
    ) {
        return attemptService.listAttempts(principal.userId(), surveyId);
    }

    @GetMapping("/active")
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
