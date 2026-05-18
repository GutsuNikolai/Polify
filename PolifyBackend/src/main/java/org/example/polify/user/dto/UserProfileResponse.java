package org.example.polify.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import org.example.polify.user.Gender;
import org.example.polify.user.Role;

@Schema(description = "User profile data for the current authenticated user.")
public record UserProfileResponse(
    @Schema(description = "User id.", example = "1")
    Long id,
    @Schema(description = "Login.", example = "alex123")
    String login,
    @Schema(description = "Optional email.", example = "alex@example.com", nullable = true)
    String email,
    @Schema(description = "Phone number (E.164).", example = "+37369123456")
    String phoneNumber,
    @Schema(description = "Optional full name.", example = "Alex Popescu", nullable = true)
    String fullName,
    @Schema(description = "Optional gender.", example = "MALE", nullable = true)
    Gender gender,
    @Schema(description = "Optional birth date.", example = "1999-12-31", nullable = true)
    LocalDate birthDate,
    @Schema(description = "Optional country.", example = "MD", nullable = true)
    String country,
    @Schema(description = "Optional city.", example = "Chisinau", nullable = true)
    String city,
    @Schema(description = "User role.", example = "USER")
    Role role,
    @Schema(description = "Whether user is verified.", example = "false")
    boolean verified,
    @Schema(description = "Last active timestamp.", example = "2026-05-05T10:00:00Z")
    Instant lastActiveAt,
    @Schema(description = "Created timestamp.", example = "2026-05-01T10:00:00Z")
    Instant createdAt
) {
}
