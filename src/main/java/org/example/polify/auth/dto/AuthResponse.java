package org.example.polify.auth.dto;

public class AuthResponse {
    private Long userId;
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

