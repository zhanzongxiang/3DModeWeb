package com.modelhub.backend.service.admin;

import java.util.Set;

public interface AccessControlService {
    Set<String> listUserRoleCodes(Long userId);

    Set<String> listUserApiPermCodes(Long userId);

    DataScopeProfile resolveDataScope(Long userId, Long userOrgId);

    boolean isSuperAdmin(Long userId);
}
