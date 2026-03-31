package com.modelhub.backend.dto.admin.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserResetPasswordRequest {
    @NotBlank(message = "new password is required")
    @Size(min = 6, max = 128, message = "password length must be between 6 and 128")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
