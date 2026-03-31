package com.modelhub.backend.controller;

import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.dto.admin.org.OrgCreateRequest;
import com.modelhub.backend.dto.admin.org.OrgStatusUpdateRequest;
import com.modelhub.backend.dto.admin.org.OrgTreeNode;
import com.modelhub.backend.dto.admin.org.OrgUpdateRequest;
import com.modelhub.backend.service.admin.OrgAdminService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orgs")
@Tag(name = "AdminOrgs", description = "Organization management APIs")
public class OrgController {
    private final OrgAdminService orgAdminService;

    public OrgController(OrgAdminService orgAdminService) {
        this.orgAdminService = orgAdminService;
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('sys:org:list')")
    @Operation(summary = "Get organization tree", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<List<OrgTreeNode>> tree() {
        return ApiResponse.success(orgAdminService.tree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('sys:org:create')")
    @Operation(summary = "Create organization node", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> create(@Valid @RequestBody OrgCreateRequest request) {
        orgAdminService.create(request);
        return ApiResponse.success("create success", null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:org:update')")
    @Operation(summary = "Update organization node", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody OrgUpdateRequest request) {
        orgAdminService.update(id, request);
        return ApiResponse.success("update success", null);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('sys:org:status')")
    @Operation(summary = "Update organization status", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody OrgStatusUpdateRequest request) {
        orgAdminService.updateStatus(id, request.getStatus());
        return ApiResponse.success("update status success", null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('sys:org:delete')")
    @Operation(summary = "Delete organization node", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> delete(@PathVariable Long id) {
        orgAdminService.delete(id);
        return ApiResponse.success("delete success", null);
    }
}
