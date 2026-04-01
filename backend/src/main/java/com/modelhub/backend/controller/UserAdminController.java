package com.modelhub.backend.controller;

import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.admin.PageResult;
import com.modelhub.backend.dto.admin.user.AdminUserCreateRequest;
import com.modelhub.backend.dto.admin.user.AdminUserQuery;
import com.modelhub.backend.dto.admin.user.AdminUserUpdateRequest;
import com.modelhub.backend.dto.admin.user.AdminUserView;
import com.modelhub.backend.dto.admin.user.UserResetPasswordRequest;
import com.modelhub.backend.dto.admin.user.UserRoleAssignRequest;
import com.modelhub.backend.dto.admin.user.UserStatusUpdateRequest;
import com.modelhub.backend.security.AuthenticatedUser;
import com.modelhub.backend.service.admin.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "AdminUsers", description = "User management APIs")
public class UserAdminController {
    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sys:user:list')")
    @Operation(summary = "List users", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<PageResult<AdminUserView>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer status,
            @Parameter(hidden = true) Authentication authentication
    ) {
        AdminUserQuery query = new AdminUserQuery();
        query.setPage(page);
        query.setSize(size);
        query.setKeyword(keyword);
        query.setOrgId(orgId);
        query.setStatus(status);
        return ApiResponse.success(userAdminService.list(query, requireUser(authentication)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:user:create')")
    @Operation(summary = "Create user", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> create(@Valid @RequestBody AdminUserCreateRequest request) {
        userAdminService.create(request);
        return ApiResponse.success("create success", null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:user:update')")
    @Operation(summary = "Update user profile", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequest request) {
        userAdminService.update(id, request);
        return ApiResponse.success("update success", null);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sys:user:status')")
    @Operation(summary = "Update user status", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody UserStatusUpdateRequest request) {
        userAdminService.updateStatus(id, request.getStatus());
        return ApiResponse.success("update status success", null);
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('sys:user:reset-password')")
    @Operation(summary = "Reset user password", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody UserResetPasswordRequest request) {
        userAdminService.resetPassword(id, request.getNewPassword());
        return ApiResponse.success("reset password success", null);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('sys:user:assign-role')")
    @Operation(summary = "Assign user roles", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> assignRoles(@PathVariable Long id, @RequestBody UserRoleAssignRequest request) {
        userAdminService.assignRoles(id, request.getRoleIds());
        return ApiResponse.success("assign roles success", null);
    }

    private AuthenticatedUser requireUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        if (user.getId() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
        return user;
    }
}
