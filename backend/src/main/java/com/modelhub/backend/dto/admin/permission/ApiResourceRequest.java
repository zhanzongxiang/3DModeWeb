package com.modelhub.backend.dto.admin.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ApiResourceRequest {
    @NotBlank(message = "api name is required")
    @Size(max = 128, message = "api name too long")
    private String name;

    @NotBlank(message = "api path is required")
    @Size(max = 255, message = "api path too long")
    private String path;

    @NotBlank(message = "method is required")
    @Size(max = 16, message = "method too long")
    private String method;

    @NotBlank(message = "perm code is required")
    @Size(max = 128, message = "perm code too long")
    private String permCode;

    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getPermCode() {
        return permCode;
    }

    public void setPermCode(String permCode) {
        this.permCode = permCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
