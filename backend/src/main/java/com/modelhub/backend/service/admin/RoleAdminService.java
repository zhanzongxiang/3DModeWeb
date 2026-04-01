package com.modelhub.backend.service.admin;

import com.modelhub.backend.dto.admin.PageResult;
import com.modelhub.backend.dto.admin.role.RoleCreateRequest;
import com.modelhub.backend.dto.admin.role.RolePermissionUpdateRequest;
import com.modelhub.backend.dto.admin.role.RoleUpdateRequest;
import com.modelhub.backend.dto.admin.role.RoleView;

public interface RoleAdminService {
    PageResult<RoleView> list(long page, long size, String keyword);

    void create(RoleCreateRequest request);

    void update(Long id, RoleUpdateRequest request);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    RoleView detail(Long id);

    void updatePermissions(Long id, RolePermissionUpdateRequest request);
}
