package com.modelhub.backend.controller;

import com.modelhub.backend.annotation.RateLimiter;
import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.dto.auth.LoginRequest;
import com.modelhub.backend.dto.auth.LoginResponse;
import com.modelhub.backend.dto.auth.RegisterRequest;
import com.modelhub.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("register success", null);
    }

    @RateLimiter(keyPrefix = "ratelimit:login", maxRequests = 5, windowSeconds = 60)
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}

