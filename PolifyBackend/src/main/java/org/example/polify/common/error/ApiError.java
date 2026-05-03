package org.example.polify.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    name = "ApiError",
    description = "Unified error response format for all API errors."
)
public record ApiError(
    @Schema(description = "UTC timestamp when the error was produced (ISO-8601).", example = "2026-04-28T14:30:12.123Z")
    String timestamp,
    @Schema(description = "HTTP status code.", example = "409")
    int status,
    @Schema(description = "HTTP status reason phrase.", example = "Conflict")
    String error,
    @Schema(description = "Stable internal error code (for client logic).", example = "ATTEMPT_NOT_ALLOWED")
    ErrorCode code,
    @Schema(description = "Human-readable message safe to show to user (no sensitive details).", example = "You already completed this survey")
    String message,
    @Schema(description = "Request path.", example = "/attempts/123/complete")
    String path,
    @Schema(description = "Correlation id for logs and troubleshooting. Also returned in response header X-Request-Id.", example = "7fd7c4b3-1798-4a7a-b2be-8c17b0e1b9df")
    String requestId,
    @Schema(description = "Optional list of field-level validation errors. Empty for non-validation errors.")
    List<FieldErrorResponse> details
) {
}
