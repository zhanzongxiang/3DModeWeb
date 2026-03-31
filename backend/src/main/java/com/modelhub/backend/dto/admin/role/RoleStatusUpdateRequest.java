package com.modelhub.backend.dto.admin.role;

import jakarta.validation.constraints.NotNull;

public class RoleStatusUpdateRequest {
    @NotNull(message = "status is required")
    private Integer status;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
