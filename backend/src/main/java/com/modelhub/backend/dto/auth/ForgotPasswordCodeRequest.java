package com.modelhub.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordCodeRequest {
    @NotBlank(message = "username is required")
    private String username;
    private String captchaToken;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }
}
