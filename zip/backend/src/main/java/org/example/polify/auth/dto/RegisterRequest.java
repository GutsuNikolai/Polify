package org.example.polify.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Registration request.")
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    @Schema(description = "Unique login/username.", example = "alex123")
    private String login;

    @NotBlank
    @Size(min = 8, max = 200)
    @Schema(description = "Plain password (min 8). Never logged.", example = "Password123!")
    private String password;

    @Email
    @Size(max = 320)
    @Schema(description = "Optional email.", example = "alex@example.com", nullable = true)
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{9,14}$")
    @Schema(description = "Phone number in E.164 format.", example = "+37369123456")
    private String phoneNumber;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
