package com.modelhub.backend.service;

import com.modelhub.backend.dto.auth.LoginRequest;
import com.modelhub.backend.dto.auth.LoginResponse;
import com.modelhub.backend.dto.auth.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);
}

