package org.example.polify.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request.")
public class LoginRequest {
    @NotBlank
    @Schema(description = "User login.", example = "alex123")
    private String login;

    @NotBlank
    @Schema(description = "Plain password.", example = "Password123!")
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
