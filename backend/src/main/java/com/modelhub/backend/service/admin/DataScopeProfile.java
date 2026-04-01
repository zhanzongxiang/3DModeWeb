package com.modelhub.backend.service.admin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DataScopeProfile {
    private final DataScopeType scopeType;
    private final Set<Long> orgIds;

    public DataScopeProfile(DataScopeType scopeType, Set<Long> orgIds) {
        this.scopeType = scopeType;
        this.orgIds = orgIds == null ? Collections.emptySet() : new HashSet<>(orgIds);
    }

    public DataScopeType getScopeType() {
        return scopeType;
    }

    public Set<Long> getOrgIds() {
        return Collections.unmodifiableSet(orgIds);
    }

    public boolean isAll() {
        return scopeType == DataScopeType.ALL;
    }
}
