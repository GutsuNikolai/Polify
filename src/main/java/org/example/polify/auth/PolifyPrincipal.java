package org.example.polify.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authenticated principal extracted from JWT.")
public record PolifyPrincipal(
    @Schema(description = "User id.", example = "1")
    Long userId,
    @Schema(description = "Login.", example = "alex123")
    String login
) {
}
