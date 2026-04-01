package com.modelhub.backend.controller;

import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.dto.oss.OssUploadResponse;
import com.modelhub.backend.service.OssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/oss")
@Tag(name = "OSS", description = "Object storage upload APIs")
public class OssController {
    private final OssService ossService;

    public OssController(OssService ossService) {
        this.ossService = ossService;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload an image file to OSS", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<OssUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        String url = ossService.uploadImage(file);
        return ApiResponse.success(new OssUploadResponse(url));
    }
}
