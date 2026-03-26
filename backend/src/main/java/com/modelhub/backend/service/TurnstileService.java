package com.modelhub.backend.service;

public interface TurnstileService {
    void verify(String token, String remoteIp);
}
