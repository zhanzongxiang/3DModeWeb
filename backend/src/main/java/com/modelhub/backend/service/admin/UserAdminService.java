package com.modelhub.backend.service.admin;

import com.modelhub.backend.dto.admin.PageResult;
import com.modelhub.backend.dto.admin.user.AdminUserCreateRequest;
import com.modelhub.backend.dto.admin.user.AdminUserQuery;
import com.modelhub.backend.dto.admin.user.AdminUserUpdateRequest;
import com.modelhub.backend.dto.admin.user.AdminUserView;
import com.modelhub.backend.security.AuthenticatedUser;

import java.util.List;

public interface UserAdminService {
    PageResult<AdminUserView> list(AdminUserQuery query, AuthenticatedUser currentUser);

    void create(AdminUserCreateRequest request);

    void update(Long id, AdminUserUpdateRequest request);

    void updateStatus(Long id, Integer status);

    void resetPassword(Long id, String newPassword);

    void assignRoles(Long id, List<Long> roleIds);
}
