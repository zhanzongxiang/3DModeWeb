package com.modelhub.backend.controller;

import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.model.ModelListQuery;
import com.modelhub.backend.dto.model.ModelListResponse;
import com.modelhub.backend.dto.model.ModelUploadRequest;
import com.modelhub.backend.security.AuthenticatedUser;
import com.modelhub.backend.service.ModelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/models")
public class ModelController {
    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @PostMapping
    public ApiResponse<Void> upload(@Valid @RequestBody ModelUploadRequest request, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        if (user.getId() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }
        modelService.upload(user.getId(), request);
        return ApiResponse.success("upload success", null);
    }

    @GetMapping
    public ApiResponse<ModelListResponse> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, name = "artwork_name") String artworkName
    ) {
        ModelListQuery query = new ModelListQuery();
        query.setPage(page);
        query.setSize(size);
        query.setType(type);
        query.setName(name);
        query.setArtworkName(artworkName);
        return ApiResponse.success(modelService.list(query));
    }
}
