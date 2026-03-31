package com.modelhub.backend.dto.admin.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoleUpdateRequest {
    @NotBlank(message = "role name is required")
    @Size(max = 64, message = "role name too long")
    private String name;

    @Size(max = 255, message = "description too long")
    private String description;

    private String dataScopeType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataScopeType() {
        return dataScopeType;
    }

    public void setDataScopeType(String dataScopeType) {
        this.dataScopeType = dataScopeType;
    }
}
