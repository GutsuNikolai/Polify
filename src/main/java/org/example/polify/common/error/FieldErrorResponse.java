package org.example.polify.common.error;

import io.swagger.v3.oas.annotations.media.Schema;

public record FieldErrorResponse(
    @Schema(description = "Name of invalid field / parameter.", example = "phoneNumber")
    String field,
    @Schema(description = "Validation message for the field.", example = "must match \"^\\\\+[1-9]\\\\d{9,14}$\"")
    String message
) {
}
