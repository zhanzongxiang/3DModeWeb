package com.modelhub.backend.security;

public class AuthenticatedUser {
    private final Long id;
    private final String username;
    private final Long orgId;

    public AuthenticatedUser(Long id, String username, Long orgId) {
        this.id = id;
        this.username = username;
        this.orgId = orgId;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Long getOrgId() {
        return orgId;
    }
}
