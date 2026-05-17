package org.example.polify.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT access token.")
public class AuthResponse {
    @Schema(description = "User id.", example = "1")
    private Long userId;

    @Schema(description = "JWT access token. Send as: Authorization: Bearer <token>.", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    public AuthResponse(Long userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
