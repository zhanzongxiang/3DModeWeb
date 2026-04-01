package com.modelhub.backend.controller;

import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.collection.CollectionToggleRequest;
import com.modelhub.backend.dto.collection.CollectionToggleResponse;
import com.modelhub.backend.entity.TbCollection;
import com.modelhub.backend.security.AuthenticatedUser;
import com.modelhub.backend.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@Tag(name = "Collections", description = "Favorite collection APIs")
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping("/toggle")
    @Operation(summary = "Toggle collection status for a model", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<CollectionToggleResponse> toggle(
            @Valid @RequestBody CollectionToggleRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        AuthenticatedUser user = requireUser(authentication);
        boolean collected = collectionService.toggle(user.getId(), request.getModelId());
        return ApiResponse.success(new CollectionToggleResponse(collected));
    }

    @GetMapping("/status")
    @Operation(summary = "Check whether current user collected a model", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<CollectionToggleResponse> status(
            @RequestParam("modelId") Long modelId,
            @Parameter(hidden = true) Authentication authentication
    ) {
        AuthenticatedUser user = requireUser(authentication);
        boolean collected = collectionService.isCollected(user.getId(), modelId);
        return ApiResponse.success(new CollectionToggleResponse(collected));
    }

    @GetMapping("/me")
    @Operation(summary = "List current user's collections", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<List<TbCollection>> myCollections(@Parameter(hidden = true) Authentication authentication) {
        AuthenticatedUser user = requireUser(authentication);
        return ApiResponse.success(collectionService.listByUserId(user.getId()));
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
