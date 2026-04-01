package com.modelhub.backend.controller;

import com.modelhub.backend.annotation.RateLimiter;
import com.modelhub.backend.common.ApiResponse;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.auth.AccountSecurityProfileResponse;
import com.modelhub.backend.dto.auth.CancelAccountRequest;
import com.modelhub.backend.dto.auth.ChangePasswordRequest;
import com.modelhub.backend.dto.auth.ForgotPasswordCodeRequest;
import com.modelhub.backend.dto.auth.ForgotPasswordCodeResponse;
import com.modelhub.backend.dto.auth.ForgotPasswordResetRequest;
import com.modelhub.backend.dto.auth.LoginRequest;
import com.modelhub.backend.dto.auth.LoginResponse;
import com.modelhub.backend.dto.auth.RegisterRequest;
import com.modelhub.backend.dto.auth.SendContactVerifyCodeRequest;
import com.modelhub.backend.dto.auth.VerifyContactCodeRequest;
import com.modelhub.backend.security.AuthenticatedUser;
import com.modelhub.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication APIs")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("register success", null);
    }

    @RateLimiter(keyPrefix = "ratelimit:login", maxRequests = 5, windowSeconds = 60)
    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT token")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @RateLimiter(keyPrefix = "ratelimit:forgot:code", maxRequests = 3, windowSeconds = 60)
    @PostMapping("/forgot-password/code")
    @Operation(summary = "Send forgot-password verification code")
    public ApiResponse<ForgotPasswordCodeResponse> sendForgotPasswordCode(
            @Valid @RequestBody ForgotPasswordCodeRequest request
    ) {
        return ApiResponse.success(authService.sendForgotPasswordCode(request));
    }

    @RateLimiter(keyPrefix = "ratelimit:forgot:reset", maxRequests = 5, windowSeconds = 60)
    @PostMapping("/forgot-password/reset")
    @Operation(summary = "Reset password with verification code")
    public ApiResponse<Void> resetPasswordByCode(@Valid @RequestBody ForgotPasswordResetRequest request) {
        authService.resetPasswordByCode(request);
        return ApiResponse.success("password reset success", null);
    }

    @GetMapping("/security/profile")
    @Operation(summary = "Get account security profile", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<AccountSecurityProfileResponse> getSecurityProfile(
            @Parameter(hidden = true) Authentication authentication
    ) {
        AuthenticatedUser user = requireUser(authentication);
        return ApiResponse.success(authService.getAccountSecurityProfile(user.getId()));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change current password", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        AuthenticatedUser user = requireUser(authentication);
        authService.changePassword(user.getId(), request);
        return ApiResponse.success("password updated", null);
    }

    @RateLimiter(keyPrefix = "ratelimit:contact:code", maxRequests = 3, windowSeconds = 60)
    @PostMapping("/contact-verification/code")
    @Operation(summary = "Send email/mobile verification code", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<ForgotPasswordCodeResponse> sendContactVerificationCode(
            @Valid @RequestBody SendContactVerifyCodeRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        AuthenticatedUser user = requireUser(authentication);
        return ApiResponse.success(authService.sendContactVerifyCode(user.getId(), request));
    }

    @PostMapping("/contact-verification/verify")
    @Operation(summary = "Verify email/mobile with code", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> verifyContactCode(
            @Valid @RequestBody VerifyContactCodeRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        AuthenticatedUser user = requireUser(authentication);
        authService.verifyContactCode(user.getId(), request);
        return ApiResponse.success("contact verified", null);
    }

    @PostMapping("/cancel-account")
    @Operation(summary = "Cancel current account", security = {@SecurityRequirement(name = "bearerAuth")})
    public ApiResponse<Void> cancelAccount(
            @Valid @RequestBody CancelAccountRequest request,
            @Parameter(hidden = true) Authentication authentication
    ) {
        AuthenticatedUser user = requireUser(authentication);
        authService.cancelAccount(user.getId(), request);
        return ApiResponse.success("account cancelled", null);
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
