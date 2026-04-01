package com.modelhub.backend.dto.admin.role;

import java.util.ArrayList;
import java.util.List;

public class RolePermissionUpdateRequest {
    private String dataScopeType;
    private List<Long> menuIds = new ArrayList<>();
    private List<Long> apiIds = new ArrayList<>();
    private List<Long> customOrgIds = new ArrayList<>();

    public String getDataScopeType() {
        return dataScopeType;
    }

    public void setDataScopeType(String dataScopeType) {
        this.dataScopeType = dataScopeType;
    }

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }

    public List<Long> getApiIds() {
        return apiIds;
    }

    public void setApiIds(List<Long> apiIds) {
        this.apiIds = apiIds;
    }

    public List<Long> getCustomOrgIds() {
        return customOrgIds;
    }

    public void setCustomOrgIds(List<Long> customOrgIds) {
        this.customOrgIds = customOrgIds;
    }
}
