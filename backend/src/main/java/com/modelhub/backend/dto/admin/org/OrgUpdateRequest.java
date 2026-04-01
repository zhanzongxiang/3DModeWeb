package com.modelhub.backend.dto.admin.org;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrgUpdateRequest {
    private Long parentId;

    @NotBlank(message = "org name is required")
    @Size(max = 64, message = "org name too long")
    private String name;

    @Size(max = 64, message = "org code too long")
    private String code;

    private Long leaderUserId;
    private Integer sort;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getLeaderUserId() {
        return leaderUserId;
    }

    public void setLeaderUserId(Long leaderUserId) {
        this.leaderUserId = leaderUserId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }
}
