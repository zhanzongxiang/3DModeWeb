package com.modelhub.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "username required")
    @Size(min = 3, max = 32, message = "username length should be 3-32")
    private String username;

    @NotBlank(message = "password required")
    @Size(min = 6, max = 64, message = "password length should be 6-64")
    private String password;

    private String captchaToken;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }
}
