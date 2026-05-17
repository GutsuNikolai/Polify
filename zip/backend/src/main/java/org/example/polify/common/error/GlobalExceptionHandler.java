package org.example.polify.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.example.polify.attempt.AttemptNotAllowedException;
import org.example.polify.attempt.AttemptNotFoundException;
import org.example.polify.attempt.AttemptSurveyNotFoundException;
import org.example.polify.attempt.AttemptValidationException;
import org.example.polify.attempt.QuestionNotFoundException;
import org.example.polify.auth.DuplicateFieldException;
import org.example.polify.auth.InvalidCredentialsException;
import org.example.polify.survey.SurveyNotFoundException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        List<FieldErrorResponse> details = ex.getBindingResult().getFieldErrors().stream()
            .map(this::toFieldError)
            .toList();

        return buildError(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_FAILED,
            "Request validation failed",
            request,
            details
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleConstraintViolation(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        List<FieldErrorResponse> details = ex.getConstraintViolations().stream()
            .map(v -> new FieldErrorResponse(
                v.getPropertyPath() == null ? null : v.getPropertyPath().toString(),
                v.getMessage()
            ))
            .toList();

        return buildError(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_FAILED,
            "Request validation failed",
            request,
            details
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleNotReadable(
        HttpMessageNotReadableException ex,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.BAD_REQUEST,
            ErrorCode.MALFORMED_JSON,
            "Malformed JSON request",
            request,
            List.of()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleMissingParam(
        MissingServletRequestParameterException ex,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_FAILED,
            "Missing required parameter: " + ex.getParameterName(),
            request,
            List.of()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request
    ) {
        String name = ex.getName();
        String message = "Invalid value for parameter: " + (name == null ? "unknown" : name);
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.TYPE_MISMATCH, message, request, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.METHOD_NOT_ALLOWED,
            ErrorCode.METHOD_NOT_ALLOWED,
            "Method not allowed",
            request,
            List.of()
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleNoHandlerFound(
        NoHandlerFoundException ex,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.NOT_FOUND,
            ErrorCode.ENDPOINT_NOT_FOUND,
            "Endpoint not found",
            request,
            List.of()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleInvalidCredentials(
        InvalidCredentialsException ex,
        HttpServletRequest request
    ) {
        return buildError(
            HttpStatus.UNAUTHORIZED,
            ErrorCode.INVALID_CREDENTIALS,
            "Invalid credentials",
            request,
            List.of()
        );
    }

    @ExceptionHandler(DuplicateFieldException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleDuplicate(
        DuplicateFieldException ex,
        HttpServletRequest request
    ) {
        ErrorCode code = switch (ex.getField()) {
            case "email" -> ErrorCode.EMAIL_ALREADY_EXISTS;
            case "phoneNumber" -> ErrorCode.PHONE_ALREADY_EXISTS;
            case "login" -> ErrorCode.LOGIN_ALREADY_EXISTS;
            default -> ErrorCode.VALIDATION_FAILED;
        };

        // Avoid leaking DB details.
        String message = switch (code) {
            case EMAIL_ALREADY_EXISTS -> "User with this email already exists";
            case PHONE_ALREADY_EXISTS -> "User with this phone number already exists";
            case LOGIN_ALREADY_EXISTS -> "User with this login already exists";
            default -> "Duplicate value";
        };

        return buildError(HttpStatus.CONFLICT, code, message, request, List.of());
    }

    @ExceptionHandler(SurveyNotFoundException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleSurveyNotFound(
        SurveyNotFoundException ex,
        HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ErrorCode.SURVEY_NOT_FOUND, "Survey not found", request, List.of());
    }

    @ExceptionHandler(AttemptSurveyNotFoundException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleAttemptSurveyNotFound(
        AttemptSurveyNotFoundException ex,
        HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ErrorCode.SURVEY_NOT_FOUND, "Survey not found", request, List.of());
    }

    @ExceptionHandler(AttemptNotFoundException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleAttemptNotFound(
        AttemptNotFoundException ex,
        HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ErrorCode.ATTEMPT_NOT_FOUND, "Attempt not found", request, List.of());
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleQuestionNotFound(
        QuestionNotFoundException ex,
        HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ErrorCode.QUESTION_NOT_FOUND, "Question not found", request, List.of());
    }

    @ExceptionHandler(AttemptNotAllowedException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleAttemptNotAllowed(
        AttemptNotAllowedException ex,
        HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, ErrorCode.ATTEMPT_NOT_ALLOWED, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(AttemptValidationException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleAttemptValidation(
        AttemptValidationException ex,
        HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, ErrorCode.ATTEMPT_VALIDATION, ex.getMessage(), request, List.of());
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<ApiError> handleFallback(Exception ex, HttpServletRequest request) {
        // No stacktrace to client.
        return buildError(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.INTERNAL_ERROR,
            "Unexpected error",
            request,
            List.of()
        );
    }

    private org.springframework.http.ResponseEntity<ApiError> buildError(
        HttpStatus status,
        ErrorCode code,
        String message,
        HttpServletRequest request,
        List<FieldErrorResponse> details
    ) {
        ApiError body = new ApiError(
            Instant.now().toString(),
            status.value(),
            status.getReasonPhrase(),
            code,
            message,
            request.getRequestURI(),
            MDC.get("requestId"),
            details == null ? List.of() : details
        );
        return org.springframework.http.ResponseEntity.status(status).body(body);
    }

    private FieldErrorResponse toFieldError(FieldError fe) {
        return new FieldErrorResponse(fe.getField(), fe.getDefaultMessage());
    }
}
