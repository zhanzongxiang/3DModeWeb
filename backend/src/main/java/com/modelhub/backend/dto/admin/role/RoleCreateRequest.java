package com.modelhub.backend.dto.admin.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoleCreateRequest {
    @NotBlank(message = "role name is required")
    @Size(max = 64, message = "role name too long")
    private String name;

    @NotBlank(message = "role code is required")
    @Size(max = 64, message = "role code too long")
    private String code;

    @Size(max = 255, message = "description too long")
    private String description;

    private Integer status;
    private String dataScopeType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDataScopeType() {
        return dataScopeType;
    }

    public void setDataScopeType(String dataScopeType) {
        this.dataScopeType = dataScopeType;
    }
}
