package org.example.polify.common.error;

import java.util.List;

public record ApiError(
    String timestamp,
    int status,
    String error,
    ErrorCode code,
    String message,
    String path,
    String requestId,
    List<FieldErrorResponse> details
) {
}

