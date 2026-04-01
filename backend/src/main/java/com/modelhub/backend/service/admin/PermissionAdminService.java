package com.modelhub.backend.service.admin;

import com.modelhub.backend.dto.admin.permission.ApiResourceRequest;
import com.modelhub.backend.dto.admin.permission.MenuResourceRequest;
import com.modelhub.backend.entity.SysApiResource;
import com.modelhub.backend.entity.SysMenu;

import java.util.List;

public interface PermissionAdminService {
    List<SysMenu> listMenus();

    List<SysApiResource> listApis();

    void createMenu(MenuResourceRequest request);

    void updateMenu(Long id, MenuResourceRequest request);

    void createApi(ApiResourceRequest request);

    void updateApi(Long id, ApiResourceRequest request);
}
