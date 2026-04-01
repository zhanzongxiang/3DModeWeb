package com.modelhub.backend.controller;

import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.dto.admin.PageResult;
import com.modelhub.backend.dto.admin.role.RoleCreateRequest;
import com.modelhub.backend.dto.admin.role.RolePermissionUpdateRequest;
import com.modelhub.backend.dto.admin.role.RoleStatusUpdateRequest;
import com.modelhub.backend.dto.admin.role.RoleUpdateRequest;
import com.modelhub.backend.dto.admin.role.RoleView;
import com.modelhub.backend.service.admin.RoleAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/admin/roles")
@Tag(name = "AdminRoles", description = "Role management APIs")
public class RoleController {
    private final RoleAdminService roleAdminService;

    public RoleController(RoleAdminService roleAdminService) {
        this.roleAdminService = roleAdminService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('sys:role:list')")
    @Operation(summary = "List roles", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<PageResult<RoleView>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(roleAdminService.list(page, size, keyword));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:list')")
    @Operation(summary = "Get role detail", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<RoleView> detail(@PathVariable Long id) {
        return ApiResponse.success(roleAdminService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:role:create')")
    @Operation(summary = "Create role", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> create(@Valid @RequestBody RoleCreateRequest request) {
        roleAdminService.create(request);
        return ApiResponse.success("create success", null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:update')")
    @Operation(summary = "Update role", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        roleAdminService.update(id, request);
        return ApiResponse.success("update success", null);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sys:role:status')")
    @Operation(summary = "Update role status", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody RoleStatusUpdateRequest request) {
        roleAdminService.updateStatus(id, request.getStatus());
        return ApiResponse.success("update status success", null);
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('sys:role:assign-permission')")
    @Operation(summary = "Update role permissions", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> updatePermissions(@PathVariable Long id, @RequestBody RolePermissionUpdateRequest request) {
        roleAdminService.updatePermissions(id, request);
        return ApiResponse.success("update permissions success", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    @Operation(summary = "Delete role", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleAdminService.delete(id);
        return ApiResponse.success("delete success", null);
    }
}
