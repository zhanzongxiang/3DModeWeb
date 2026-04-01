package com.modelhub.backend.service;

import com.modelhub.backend.dto.auth.AccountSecurityProfileResponse;
import com.modelhub.backend.dto.auth.ChangePasswordRequest;
import com.modelhub.backend.dto.auth.ForgotPasswordCodeRequest;
import com.modelhub.backend.dto.auth.ForgotPasswordCodeResponse;
import com.modelhub.backend.dto.auth.ForgotPasswordResetRequest;
import com.modelhub.backend.dto.auth.LoginRequest;
import com.modelhub.backend.dto.auth.LoginResponse;
import com.modelhub.backend.dto.auth.CancelAccountRequest;
import com.modelhub.backend.dto.auth.RegisterRequest;
import com.modelhub.backend.dto.auth.SendContactVerifyCodeRequest;
import com.modelhub.backend.dto.auth.VerifyContactCodeRequest;

public interface AuthService {
    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    ForgotPasswordCodeResponse sendForgotPasswordCode(ForgotPasswordCodeRequest request);

    void resetPasswordByCode(ForgotPasswordResetRequest request);

    AccountSecurityProfileResponse getAccountSecurityProfile(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);

    ForgotPasswordCodeResponse sendContactVerifyCode(Long userId, SendContactVerifyCodeRequest request);

    void verifyContactCode(Long userId, VerifyContactCodeRequest request);

    void cancelAccount(Long userId, CancelAccountRequest request);
}
