package com.modelhub.backend.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.modelhub.backend.entity.SysOrg;
import com.modelhub.backend.mapper.SysApiResourceMapper;
import com.modelhub.backend.mapper.SysOrgMapper;
import com.modelhub.backend.mapper.SysRoleMapper;
import com.modelhub.backend.service.admin.AccessControlService;
import com.modelhub.backend.service.admin.DataScopeProfile;
import com.modelhub.backend.service.admin.DataScopeType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccessControlServiceImpl implements AccessControlService {
    private static final String SUPER_ADMIN_CODE = "SUPER_ADMIN";

    private final SysRoleMapper roleMapper;
    private final SysApiResourceMapper apiResourceMapper;
    private final SysOrgMapper orgMapper;

    public AccessControlServiceImpl(
            SysRoleMapper roleMapper,
            SysApiResourceMapper apiResourceMapper,
            SysOrgMapper orgMapper
    ) {
        this.roleMapper = roleMapper;
        this.apiResourceMapper = apiResourceMapper;
        this.orgMapper = orgMapper;
    }

    @Override
    public Set<String> listUserRoleCodes(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        List<String> rows;
        try {
            rows = roleMapper.selectRoleCodesByUserId(userId);
        } catch (Exception ignored) {
            rows = Collections.emptyList();
        }
        Set<String> codes = new LinkedHashSet<>(rows);
        if (userId == 1L) {
            codes.add(SUPER_ADMIN_CODE);
        }
        return codes;
    }

    @Override
    public Set<String> listUserApiPermCodes(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        try {
            if (isSuperAdmin(userId)) {
                return new LinkedHashSet<>(apiResourceMapper.selectAllEnabledPermCodes());
            }
            return new LinkedHashSet<>(roleMapper.selectApiPermCodesByUserId(userId));
        } catch (Exception ignored) {
            return Collections.emptySet();
        }
    }

    @Override
    public DataScopeProfile resolveDataScope(Long userId, Long userOrgId) {
        if (userId == null) {
            return new DataScopeProfile(DataScopeType.SELF, Collections.emptySet());
        }
        if (isSuperAdmin(userId)) {
            return new DataScopeProfile(DataScopeType.ALL, Collections.emptySet());
        }

        List<String> values;
        try {
            values = roleMapper.selectDataScopeTypesByUserId(userId);
        } catch (Exception ignored) {
            return new DataScopeProfile(DataScopeType.SELF, Collections.emptySet());
        }
        if (values == null || values.isEmpty()) {
            return new DataScopeProfile(DataScopeType.SELF, Collections.emptySet());
        }

        DataScopeType winner = DataScopeType.SELF;
        int maxScore = score(winner);
        for (String value : values) {
            DataScopeType type = DataScopeType.from(value);
            int score = score(type);
            if (score > maxScore) {
                maxScore = score;
                winner = type;
            }
        }

        if (winner == DataScopeType.ALL) {
            return new DataScopeProfile(DataScopeType.ALL, Collections.emptySet());
        }

        if (winner == DataScopeType.ORG_AND_CHILDREN) {
            if (userOrgId == null) {
                return new DataScopeProfile(DataScopeType.SELF, Collections.emptySet());
            }
            Set<Long> orgIds = new HashSet<>(listOrgWithChildren(userOrgId));
            return new DataScopeProfile(DataScopeType.ORG_AND_CHILDREN, orgIds);
        }

        if (winner == DataScopeType.ORG_ONLY) {
            if (userOrgId == null) {
                return new DataScopeProfile(DataScopeType.SELF, Collections.emptySet());
            }
            return new DataScopeProfile(DataScopeType.ORG_ONLY, Collections.singleton(userOrgId));
        }

        if (winner == DataScopeType.CUSTOM_ORG) {
            List<Long> valuesOrg;
            try {
                valuesOrg = roleMapper.selectCustomScopeOrgIdsByUserId(userId);
            } catch (Exception ignored) {
                valuesOrg = Collections.emptyList();
            }
            Set<Long> orgIds = new HashSet<>(valuesOrg);
            return new DataScopeProfile(DataScopeType.CUSTOM_ORG, orgIds);
        }

        return new DataScopeProfile(DataScopeType.SELF, Collections.emptySet());
    }

    @Override
    public boolean isSuperAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        if (userId == 1L) {
            return true;
        }
        try {
            return listUserRoleCodes(userId).contains(SUPER_ADMIN_CODE);
        } catch (Exception ignored) {
            return false;
        }
    }

    private int score(DataScopeType type) {
        switch (type) {
            case ALL:
                return 5;
            case ORG_AND_CHILDREN:
                return 4;
            case CUSTOM_ORG:
                return 3;
            case ORG_ONLY:
                return 2;
            case SELF:
            default:
                return 1;
        }
    }

    private List<Long> listOrgWithChildren(Long orgId) {
        if (orgId == null) {
            return Collections.emptyList();
        }
        List<SysOrg> rows = orgMapper.selectList(
                new LambdaQueryWrapper<SysOrg>()
                        .eq(SysOrg::getId, orgId)
                        .or(wrapper -> wrapper.apply("find_in_set({0}, ancestors)", orgId))
        );
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }
        return rows.stream().map(SysOrg::getId).collect(Collectors.toList());
    }
}
