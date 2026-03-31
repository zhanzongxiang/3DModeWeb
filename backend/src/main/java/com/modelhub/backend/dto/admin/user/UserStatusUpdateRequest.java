package com.modelhub.backend.dto.admin.user;

import jakarta.validation.constraints.NotNull;

public class UserStatusUpdateRequest {
    @NotNull(message = "status is required")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
