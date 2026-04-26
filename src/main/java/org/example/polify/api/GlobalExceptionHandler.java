package org.example.polify.api;

import org.example.polify.auth.DuplicateFieldException;
import org.example.polify.auth.InvalidCredentialsException;
import org.example.polify.attempt.AttemptNotAllowedException;
import org.example.polify.attempt.AttemptNotFoundException;
import org.example.polify.attempt.AttemptSurveyNotFoundException;
import org.example.polify.attempt.AttemptValidationException;
import org.example.polify.attempt.QuestionNotFoundException;
import org.example.polify.survey.SurveyNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateFieldException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiError("DUPLICATE", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiError("INVALID_CREDENTIALS", ex.getMessage()));
    }

    @ExceptionHandler(SurveyNotFoundException.class)
    public ResponseEntity<ApiError> handleSurveyNotFound(SurveyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiError("SURVEY_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(AttemptSurveyNotFoundException.class)
    public ResponseEntity<ApiError> handleAttemptSurveyNotFound(AttemptSurveyNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiError("SURVEY_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(AttemptNotFoundException.class)
    public ResponseEntity<ApiError> handleAttemptNotFound(AttemptNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiError("ATTEMPT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ApiError> handleQuestionNotFound(QuestionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiError("QUESTION_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(AttemptNotAllowedException.class)
    public ResponseEntity<ApiError> handleAttemptNotAllowed(AttemptNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiError("ATTEMPT_NOT_ALLOWED", ex.getMessage()));
    }

    @ExceptionHandler(AttemptValidationException.class)
    public ResponseEntity<ApiError> handleAttemptValidation(AttemptValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiError("ATTEMPT_VALIDATION", ex.getMessage()));
    }
}
