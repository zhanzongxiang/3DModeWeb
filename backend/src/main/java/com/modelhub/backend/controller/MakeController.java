package com.modelhub.backend.controller;

import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.make.MakeCreateRequest;
import com.modelhub.backend.dto.make.MakeView;
import com.modelhub.backend.security.AuthenticatedUser;
import com.modelhub.backend.service.MakeService;
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
@RequestMapping("/api/makes")
public class MakeController {
    private final MakeService makeService;

    public MakeController(MakeService makeService) {
        this.makeService = makeService;
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody MakeCreateRequest request, Authentication authentication) {
        AuthenticatedUser user = requireUser(authentication);
        makeService.create(user.getId(), request);
        return ApiResponse.success("make upload success", null);
    }

    @GetMapping
    public ApiResponse<List<MakeView>> listByModel(@RequestParam("modelId") Long modelId) {
        return ApiResponse.success(makeService.listByModelId(modelId));
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

