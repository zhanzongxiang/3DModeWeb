package com.modelhub.backend.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.admin.permission.ApiResourceRequest;
import com.modelhub.backend.dto.admin.permission.MenuResourceRequest;
import com.modelhub.backend.entity.SysApiResource;
import com.modelhub.backend.entity.SysMenu;
import com.modelhub.backend.mapper.SysApiResourceMapper;
import com.modelhub.backend.mapper.SysMenuMapper;
import com.modelhub.backend.service.admin.PermissionAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PermissionAdminServiceImpl implements PermissionAdminService {
    private final SysMenuMapper menuMapper;
    private final SysApiResourceMapper apiResourceMapper;

    public PermissionAdminServiceImpl(SysMenuMapper menuMapper, SysApiResourceMapper apiResourceMapper) {
        this.menuMapper = menuMapper;
        this.apiResourceMapper = apiResourceMapper;
    }

    @Override
    public List<SysMenu> listMenus() {
        return menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .orderByAsc(SysMenu::getParentId)
                        .orderByAsc(SysMenu::getSort)
                        .orderByAsc(SysMenu::getId)
        );
    }

    @Override
    public List<SysApiResource> listApis() {
        return apiResourceMapper.selectList(
                new LambdaQueryWrapper<SysApiResource>()
                        .orderByAsc(SysApiResource::getMethod)
                        .orderByAsc(SysApiResource::getPath)
        );
    }

    @Override
    public void createMenu(MenuResourceRequest request) {
        if (StringUtils.hasText(request.getPermCode())) {
            checkMenuPermCodeUnique(request.getPermCode().trim(), null);
        }

        SysMenu row = new SysMenu();
        row.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        row.setName(request.getName().trim());
        row.setPath(request.getPath());
        row.setComponent(request.getComponent());
        row.setPermCode(StringUtils.hasText(request.getPermCode()) ? request.getPermCode().trim() : null);
        row.setSort(request.getSort() == null ? 0 : request.getSort());
        row.setVisible(request.getVisible() == null ? 1 : request.getVisible());
        row.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        menuMapper.insert(row);
    }

    @Override
    public void updateMenu(Long id, MenuResourceRequest request) {
        SysMenu row = requireMenu(id);
        if (StringUtils.hasText(request.getPermCode())) {
            checkMenuPermCodeUnique(request.getPermCode().trim(), id);
        }
        row.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        row.setName(request.getName().trim());
        row.setPath(request.getPath());
        row.setComponent(request.getComponent());
        row.setPermCode(StringUtils.hasText(request.getPermCode()) ? request.getPermCode().trim() : null);
        row.setSort(request.getSort() == null ? 0 : request.getSort());
        row.setVisible(request.getVisible() == null ? 1 : request.getVisible());
        row.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        row.setUpdateTime(LocalDateTime.now());
        menuMapper.updateById(row);
    }

    @Override
    public void createApi(ApiResourceRequest request) {
        checkApiPermCodeUnique(request.getPermCode().trim(), null);
        SysApiResource row = new SysApiResource();
        row.setName(request.getName().trim());
        row.setPath(request.getPath().trim());
        row.setMethod(request.getMethod().trim().toUpperCase());
        row.setPermCode(request.getPermCode().trim());
        row.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        apiResourceMapper.insert(row);
    }

    @Override
    public void updateApi(Long id, ApiResourceRequest request) {
        SysApiResource row = requireApi(id);
        checkApiPermCodeUnique(request.getPermCode().trim(), id);
        row.setName(request.getName().trim());
        row.setPath(request.getPath().trim());
        row.setMethod(request.getMethod().trim().toUpperCase());
        row.setPermCode(request.getPermCode().trim());
        row.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        row.setUpdateTime(LocalDateTime.now());
        apiResourceMapper.updateById(row);
    }

    private SysMenu requireMenu(Long id) {
        SysMenu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "menu not found");
        }
        return menu;
    }

    private SysApiResource requireApi(Long id) {
        SysApiResource api = apiResourceMapper.selectById(id);
        if (api == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "api resource not found");
        }
        return api;
    }

    private void checkMenuPermCodeUnique(String permCode, Long ignoreId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getPermCode, permCode);
        if (ignoreId != null) {
            wrapper.ne(SysMenu::getId, ignoreId);
        }
        SysMenu existing = menuMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "menu perm code already exists");
        }
    }

    private void checkApiPermCodeUnique(String permCode, Long ignoreId) {
        LambdaQueryWrapper<SysApiResource> wrapper = new LambdaQueryWrapper<SysApiResource>().eq(SysApiResource::getPermCode, permCode);
        if (ignoreId != null) {
            wrapper.ne(SysApiResource::getId, ignoreId);
        }
        SysApiResource existing = apiResourceMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "api perm code already exists");
        }
    }
}
