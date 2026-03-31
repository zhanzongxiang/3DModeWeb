package com.modelhub.backend.service.admin;

import com.modelhub.backend.dto.admin.org.OrgCreateRequest;
import com.modelhub.backend.dto.admin.org.OrgTreeNode;
import com.modelhub.backend.dto.admin.org.OrgUpdateRequest;

import java.util.List;

public interface OrgAdminService {
    List<OrgTreeNode> tree();

    void create(OrgCreateRequest request);

    void update(Long id, OrgUpdateRequest request);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
