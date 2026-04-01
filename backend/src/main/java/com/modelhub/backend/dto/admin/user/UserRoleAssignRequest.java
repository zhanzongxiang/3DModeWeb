package com.modelhub.backend.dto.admin.user;

import java.util.ArrayList;
import java.util.List;

public class UserRoleAssignRequest {
    private List<Long> roleIds = new ArrayList<>();

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
