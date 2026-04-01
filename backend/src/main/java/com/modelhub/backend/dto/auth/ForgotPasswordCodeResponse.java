package com.modelhub.backend.dto.auth;

public class ForgotPasswordCodeResponse {
    private long expiresInSeconds;
    private String devCode;

    public ForgotPasswordCodeResponse() {
    }

    public ForgotPasswordCodeResponse(long expiresInSeconds, String devCode) {
        this.expiresInSeconds = expiresInSeconds;
        this.devCode = devCode;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getDevCode() {
        return devCode;
    }

    public void setDevCode(String devCode) {
        this.devCode = devCode;
    }
}
