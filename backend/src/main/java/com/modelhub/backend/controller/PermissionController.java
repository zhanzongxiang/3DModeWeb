package com.modelhub.backend.controller;

import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.dto.admin.permission.ApiResourceRequest;
import com.modelhub.backend.dto.admin.permission.MenuResourceRequest;
import com.modelhub.backend.entity.SysApiResource;
import com.modelhub.backend.entity.SysMenu;
import com.modelhub.backend.service.admin.PermissionAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
@Tag(name = "AdminPermissions", description = "Permission resource APIs")
public class PermissionController {
    private final PermissionAdminService permissionAdminService;

    public PermissionController(PermissionAdminService permissionAdminService) {
        this.permissionAdminService = permissionAdminService;
    }

    @GetMapping("/menus")
    @PreAuthorize("hasAuthority('sys:permission:menu:list')")
    @Operation(summary = "List menu resources", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<List<SysMenu>> listMenus() {
        return ApiResponse.success(permissionAdminService.listMenus());
    }

    @PostMapping("/menus")
    @PreAuthorize("hasAuthority('sys:permission:menu:create')")
    @Operation(summary = "Create menu resource", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> createMenu(@Valid @RequestBody MenuResourceRequest request) {
        permissionAdminService.createMenu(request);
        return ApiResponse.success("create menu success", null);
    }

    @PutMapping("/menus/{id}")
    @PreAuthorize("hasAuthority('sys:permission:menu:update')")
    @Operation(summary = "Update menu resource", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuResourceRequest request) {
        permissionAdminService.updateMenu(id, request);
        return ApiResponse.success("update menu success", null);
    }

    @GetMapping("/apis")
    @PreAuthorize("hasAuthority('sys:permission:api:list')")
    @Operation(summary = "List API resources", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<List<SysApiResource>> listApis() {
        return ApiResponse.success(permissionAdminService.listApis());
    }

    @PostMapping("/apis")
    @PreAuthorize("hasAuthority('sys:permission:api:create')")
    @Operation(summary = "Create API resource", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> createApi(@Valid @RequestBody ApiResourceRequest request) {
        permissionAdminService.createApi(request);
        return ApiResponse.success("create api success", null);
    }

    @PutMapping("/apis/{id}")
    @PreAuthorize("hasAuthority('sys:permission:api:update')")
    @Operation(summary = "Update API resource", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> updateApi(@PathVariable Long id, @Valid @RequestBody ApiResourceRequest request) {
        permissionAdminService.updateApi(id, request);
        return ApiResponse.success("update api success", null);
    }
}
