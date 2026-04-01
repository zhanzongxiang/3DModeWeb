package com.modelhub.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class CancelAccountRequest {
    @NotBlank(message = "password is required")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
