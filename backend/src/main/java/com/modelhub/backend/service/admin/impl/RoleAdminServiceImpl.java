package com.modelhub.backend.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.admin.PageResult;
import com.modelhub.backend.dto.admin.role.RoleCreateRequest;
import com.modelhub.backend.dto.admin.role.RolePermissionUpdateRequest;
import com.modelhub.backend.dto.admin.role.RoleUpdateRequest;
import com.modelhub.backend.dto.admin.role.RoleView;
import com.modelhub.backend.entity.SysRole;
import com.modelhub.backend.entity.SysRoleApi;
import com.modelhub.backend.entity.SysRoleDataScopeOrg;
import com.modelhub.backend.entity.SysRoleMenu;
import com.modelhub.backend.entity.SysUserRole;
import com.modelhub.backend.mapper.SysRoleApiMapper;
import com.modelhub.backend.mapper.SysRoleDataScopeOrgMapper;
import com.modelhub.backend.mapper.SysRoleMapper;
import com.modelhub.backend.mapper.SysRoleMenuMapper;
import com.modelhub.backend.mapper.SysUserRoleMapper;
import com.modelhub.backend.service.admin.DataScopeType;
import com.modelhub.backend.service.admin.RoleAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleAdminServiceImpl implements RoleAdminService {
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysRoleApiMapper roleApiMapper;
    private final SysRoleDataScopeOrgMapper roleDataScopeOrgMapper;

    public RoleAdminServiceImpl(
            SysRoleMapper roleMapper,
            SysUserRoleMapper userRoleMapper,
            SysRoleMenuMapper roleMenuMapper,
            SysRoleApiMapper roleApiMapper,
            SysRoleDataScopeOrgMapper roleDataScopeOrgMapper
    ) {
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.roleApiMapper = roleApiMapper;
        this.roleDataScopeOrgMapper = roleDataScopeOrgMapper;
    }

    @Override
    public PageResult<RoleView> list(long page, long size, String keyword) {
        Page<SysRole> p = new Page<>(Math.max(page, 1), Math.max(size, 1));
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .orderByDesc(SysRole::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getName, keyword.trim()).or().like(SysRole::getCode, keyword.trim()));
        }
        Page<SysRole> result = roleMapper.selectPage(p, wrapper);
        List<RoleView> items = result.getRecords().stream().map(this::toView).collect(Collectors.toList());
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public void create(RoleCreateRequest request) {
        checkRoleCodeUnique(request.getCode().trim(), null);
        SysRole role = new SysRole();
        role.setName(request.getName().trim());
        role.setCode(request.getCode().trim());
        role.setDescription(request.getDescription());
        role.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        role.setDataScopeType(normalizeScope(request.getDataScopeType()));
        role.setIsSystem(0);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);
    }

    @Override
    @Transactional
    public void update(Long id, RoleUpdateRequest request) {
        SysRole role = requireRole(id);
        role.setName(request.getName().trim());
        role.setDescription(request.getDescription());
        role.setDataScopeType(normalizeScope(request.getDataScopeType()));
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysRole role = requireRole(id);
        role.setStatus(status == null ? 1 : status);
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysRole role = requireRole(id);
        if (role.getIsSystem() != null && role.getIsSystem() == 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "system role cannot be deleted");
        }
        Long bindCount = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        if (bindCount != null && bindCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "role is assigned to users");
        }

        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        roleApiMapper.delete(new LambdaQueryWrapper<SysRoleApi>().eq(SysRoleApi::getRoleId, id));
        roleDataScopeOrgMapper.delete(new LambdaQueryWrapper<SysRoleDataScopeOrg>().eq(SysRoleDataScopeOrg::getRoleId, id));
        roleMapper.deleteById(id);
    }

    @Override
    public RoleView detail(Long id) {
        SysRole role = requireRole(id);
        RoleView view = toView(role);
        fillPermissionBindings(view);
        return view;
    }

    @Override
    @Transactional
    public void updatePermissions(Long id, RolePermissionUpdateRequest request) {
        SysRole role = requireRole(id);
        role.setDataScopeType(normalizeScope(request.getDataScopeType()));
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.updateById(role);

        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        roleApiMapper.delete(new LambdaQueryWrapper<SysRoleApi>().eq(SysRoleApi::getRoleId, id));
        roleDataScopeOrgMapper.delete(new LambdaQueryWrapper<SysRoleDataScopeOrg>().eq(SysRoleDataScopeOrg::getRoleId, id));

        if (request.getMenuIds() != null) {
            for (Long menuId : request.getMenuIds()) {
                SysRoleMenu row = new SysRoleMenu();
                row.setRoleId(id);
                row.setMenuId(menuId);
                row.setCreateTime(LocalDateTime.now());
                roleMenuMapper.insert(row);
            }
        }
        if (request.getApiIds() != null) {
            for (Long apiId : request.getApiIds()) {
                SysRoleApi row = new SysRoleApi();
                row.setRoleId(id);
                row.setApiId(apiId);
                row.setCreateTime(LocalDateTime.now());
                roleApiMapper.insert(row);
            }
        }
        if ("CUSTOM_ORG".equalsIgnoreCase(role.getDataScopeType()) && request.getCustomOrgIds() != null) {
            for (Long orgId : request.getCustomOrgIds()) {
                SysRoleDataScopeOrg row = new SysRoleDataScopeOrg();
                row.setRoleId(id);
                row.setOrgId(orgId);
                row.setCreateTime(LocalDateTime.now());
                roleDataScopeOrgMapper.insert(row);
            }
        }
    }

    private void checkRoleCodeUnique(String code, Long ignoreId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, code);
        if (ignoreId != null) {
            wrapper.ne(SysRole::getId, ignoreId);
        }
        SysRole existing = roleMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "role code already exists");
        }
    }

    private SysRole requireRole(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "role not found");
        }
        return role;
    }

    private String normalizeScope(String value) {
        return DataScopeType.from(value).name();
    }

    private RoleView toView(SysRole role) {
        RoleView view = new RoleView();
        view.setId(role.getId());
        view.setName(role.getName());
        view.setCode(role.getCode());
        view.setDescription(role.getDescription());
        view.setStatus(role.getStatus());
        view.setDataScopeType(role.getDataScopeType());
        view.setIsSystem(role.getIsSystem());
        view.setCreateTime(role.getCreateTime());
        view.setUpdateTime(role.getUpdateTime());
        return view;
    }

    private void fillPermissionBindings(RoleView view) {
        List<Long> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, view.getId())
        ).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());

        List<Long> apiIds = roleApiMapper.selectList(
                new LambdaQueryWrapper<SysRoleApi>().eq(SysRoleApi::getRoleId, view.getId())
        ).stream().map(SysRoleApi::getApiId).collect(Collectors.toList());

        List<Long> customOrgIds = roleDataScopeOrgMapper.selectList(
                new LambdaQueryWrapper<SysRoleDataScopeOrg>().eq(SysRoleDataScopeOrg::getRoleId, view.getId())
        ).stream().map(SysRoleDataScopeOrg::getOrgId).collect(Collectors.toList());

        view.setMenuIds(menuIds == null ? new ArrayList<>() : menuIds);
        view.setApiIds(apiIds == null ? new ArrayList<>() : apiIds);
        view.setCustomOrgIds(customOrgIds == null ? new ArrayList<>() : customOrgIds);
    }
}
