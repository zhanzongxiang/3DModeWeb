package com.modelhub.backend.controller;

import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.admin.auth.AdminCurrentUserResponse;
import com.modelhub.backend.security.AuthenticatedUser;
import com.modelhub.backend.service.admin.AccessControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "AdminAuth", description = "Admin auth context APIs")
public class AdminAuthController {
    private final AccessControlService accessControlService;

    public AdminAuthController(AccessControlService accessControlService) {
        this.accessControlService = accessControlService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('sys:auth:me')")
    @Operation(summary = "Get current admin profile", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<AdminCurrentUserResponse> me(@Parameter(hidden = true) Authentication authentication) {
        AuthenticatedUser current = requireUser(authentication);
        AdminCurrentUserResponse response = new AdminCurrentUserResponse();
        response.setUserId(current.getId());
        response.setUsername(current.getUsername());
        response.setOrgId(current.getOrgId());
        response.setRoles(new ArrayList<>(accessControlService.listUserRoleCodes(current.getId())));
        response.setPermissions(new ArrayList<>(accessControlService.listUserApiPermCodes(current.getId())));
        return ApiResponse.success(response);
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
