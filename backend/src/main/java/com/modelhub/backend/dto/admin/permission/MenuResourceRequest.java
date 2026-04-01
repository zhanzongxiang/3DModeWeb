package com.modelhub.backend.dto.admin.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MenuResourceRequest {
    private Long parentId;

    @NotBlank(message = "menu name is required")
    @Size(max = 64, message = "menu name too long")
    private String name;

    @Size(max = 128, message = "menu path too long")
    private String path;

    @Size(max = 255, message = "component too long")
    private String component;

    @Size(max = 128, message = "perm code too long")
    private String permCode;

    private Integer sort;
    private Integer visible;
    private Integer status;

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

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

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getPermCode() {
        return permCode;
    }

    public void setPermCode(String permCode) {
        this.permCode = permCode;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
