package com.modelhub.backend.dto.auth;

public class LoginResponse {
    private String token;
    private long expiresAt;
    private Long userId;
    private String username;

    public LoginResponse() {
    }

    public LoginResponse(String token, long expiresAt, Long userId, String username) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.userId = userId;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

