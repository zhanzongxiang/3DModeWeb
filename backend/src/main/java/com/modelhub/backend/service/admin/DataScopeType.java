package com.modelhub.backend.service.admin;

public enum DataScopeType {
    ALL,
    ORG_AND_CHILDREN,
    ORG_ONLY,
    SELF,
    CUSTOM_ORG;

    public static DataScopeType from(String value) {
        if (value == null || value.isBlank()) {
            return SELF;
        }
        try {
            return DataScopeType.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return SELF;
        }
    }
}
