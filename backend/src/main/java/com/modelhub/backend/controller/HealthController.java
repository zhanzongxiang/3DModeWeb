package com.modelhub.backend.controller;

import com.modelhub.backend.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Service health check API")
public class HealthController {

    @GetMapping("/api/health")
    @Operation(summary = "Health check")
    public ApiResponse<String> health() {
        return ApiResponse.success("ok");
    }
}
