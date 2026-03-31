package com.modelhub.backend.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.admin.PageResult;
import com.modelhub.backend.dto.admin.user.AdminUserCreateRequest;
import com.modelhub.backend.dto.admin.user.AdminUserQuery;
import com.modelhub.backend.dto.admin.user.AdminUserUpdateRequest;
import com.modelhub.backend.dto.admin.user.AdminUserView;
import com.modelhub.backend.entity.SysRole;
import com.modelhub.backend.entity.SysUser;
import com.modelhub.backend.entity.SysUserRole;
import com.modelhub.backend.mapper.SysRoleMapper;
import com.modelhub.backend.mapper.SysUserMapper;
import com.modelhub.backend.mapper.SysUserRoleMapper;
import com.modelhub.backend.security.AuthenticatedUser;
import com.modelhub.backend.service.admin.AccessControlService;
import com.modelhub.backend.service.admin.DataScopeProfile;
import com.modelhub.backend.service.admin.DataScopeType;
import com.modelhub.backend.service.admin.UserAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAdminServiceImpl implements UserAdminService {
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccessControlService accessControlService;

    public UserAdminServiceImpl(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysUserRoleMapper userRoleMapper,
            PasswordEncoder passwordEncoder,
            AccessControlService accessControlService
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.accessControlService = accessControlService;
    }

    @Override
    public PageResult<AdminUserView> list(AdminUserQuery query, AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "unauthorized");
        }

        long pageNo = Math.max(query.getPage(), 1);
        long pageSize = Math.max(query.getSize(), 1);
        Page<SysUser> page = new Page<>(pageNo, pageSize);

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .orderByDesc(SysUser::getCreateTime);

        if (StringUtils.hasText(query.getKeyword())) {
            String text = query.getKeyword().trim();
            wrapper.and(w -> w.like(SysUser::getUsername, text)
                    .or().like(SysUser::getNickname, text)
                    .or().like(SysUser::getRealName, text));
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }

        applyDataScope(wrapper, currentUser);

        if (query.getOrgId() != null) {
            wrapper.eq(SysUser::getOrgId, query.getOrgId());
        }

        Page<SysUser> result = userMapper.selectPage(page, wrapper);
        List<AdminUserView> items = toViews(result.getRecords());
        return new PageResult<>(items, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional
    public void create(AdminUserCreateRequest request) {
        SysUser exists = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.getUsername().trim())
        );
        if (exists != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "username already exists");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : request.getUsername().trim());
        user.setRealName(request.getRealName());
        user.setMobile(request.getMobile());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setOrgId(request.getOrgId() == null ? 1L : request.getOrgId());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        assignRoles(user.getId(), request.getRoleIds());
    }

    @Override
    public void update(Long id, AdminUserUpdateRequest request) {
        SysUser user = requireUser(id);
        user.setNickname(request.getNickname());
        user.setRealName(request.getRealName());
        user.setMobile(request.getMobile());
        user.setEmail(request.getEmail());
        user.setOrgId(request.getOrgId());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = requireUser(id);
        user.setStatus(status == null ? 1 : status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser user = requireUser(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long id, List<Long> roleIds) {
        requireUser(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : new LinkedHashSet<>(roleIds)) {
            SysRole role = roleMapper.selectById(roleId);
            if (role == null) {
                continue;
            }
            SysUserRole row = new SysUserRole();
            row.setUserId(id);
            row.setRoleId(roleId);
            row.setCreateTime(LocalDateTime.now());
            userRoleMapper.insert(row);
        }
    }

    private SysUser requireUser(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "user not found");
        }
        return user;
    }

    private void applyDataScope(LambdaQueryWrapper<SysUser> wrapper, AuthenticatedUser currentUser) {
        DataScopeProfile profile = accessControlService.resolveDataScope(currentUser.getId(), currentUser.getOrgId());
        DataScopeType type = profile.getScopeType();
        if (type == DataScopeType.ALL) {
            return;
        }
        if (type == DataScopeType.SELF) {
            wrapper.eq(SysUser::getId, currentUser.getId());
            return;
        }
        Set<Long> orgIds = profile.getOrgIds();
        if (orgIds == null || orgIds.isEmpty()) {
            wrapper.eq(SysUser::getId, -1L);
            return;
        }
        wrapper.in(SysUser::getOrgId, orgIds);
    }

    private List<AdminUserView> toViews(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
        List<SysUserRole> bindings = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds)
        );

        Map<Long, List<Long>> roleIdsByUserId = new HashMap<>();
        Set<Long> allRoleIds = new LinkedHashSet<>();
        for (SysUserRole row : bindings) {
            roleIdsByUserId.computeIfAbsent(row.getUserId(), key -> new ArrayList<>()).add(row.getRoleId());
            allRoleIds.add(row.getRoleId());
        }

        Map<Long, String> roleCodeMap = new HashMap<>();
        if (!allRoleIds.isEmpty()) {
            List<SysRole> roles = roleMapper.selectBatchIds(allRoleIds);
            for (SysRole role : roles) {
                roleCodeMap.put(role.getId(), role.getCode());
            }
        }

        List<AdminUserView> result = new ArrayList<>();
        for (SysUser user : users) {
            AdminUserView view = new AdminUserView();
            view.setId(user.getId());
            view.setUsername(user.getUsername());
            view.setNickname(user.getNickname());
            view.setRealName(user.getRealName());
            view.setMobile(user.getMobile());
            view.setEmail(user.getEmail());
            view.setStatus(user.getStatus());
            view.setOrgId(user.getOrgId());
            view.setLastLoginTime(user.getLastLoginTime());
            view.setCreateTime(user.getCreateTime());
            view.setUpdateTime(user.getUpdateTime());

            List<Long> roleIds = roleIdsByUserId.getOrDefault(user.getId(), Collections.emptyList());
            view.setRoleIds(new ArrayList<>(roleIds));
            List<String> roleCodes = roleIds.stream()
                    .map(roleCodeMap::get)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toList());
            view.setRoleCodes(roleCodes);
            result.add(view);
        }
        return result;
    }
}
