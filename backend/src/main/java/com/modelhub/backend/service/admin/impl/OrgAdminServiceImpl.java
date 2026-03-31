package com.modelhub.backend.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.modelhub.backend.common.BusinessException;
import com.modelhub.backend.dto.admin.org.OrgCreateRequest;
import com.modelhub.backend.dto.admin.org.OrgTreeNode;
import com.modelhub.backend.dto.admin.org.OrgUpdateRequest;
import com.modelhub.backend.entity.SysOrg;
import com.modelhub.backend.entity.SysUser;
import com.modelhub.backend.mapper.SysOrgMapper;
import com.modelhub.backend.mapper.SysUserMapper;
import com.modelhub.backend.service.admin.OrgAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OrgAdminServiceImpl implements OrgAdminService {
    private final SysOrgMapper orgMapper;
    private final SysUserMapper userMapper;

    public OrgAdminServiceImpl(SysOrgMapper orgMapper, SysUserMapper userMapper) {
        this.orgMapper = orgMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<OrgTreeNode> tree() {
        List<SysOrg> rows = orgMapper.selectList(
                new LambdaQueryWrapper<SysOrg>()
                        .orderByAsc(SysOrg::getSort)
                        .orderByAsc(SysOrg::getId)
        );
        Map<Long, OrgTreeNode> map = new HashMap<>();
        List<OrgTreeNode> roots = new ArrayList<>();

        for (SysOrg row : rows) {
            OrgTreeNode node = toNode(row);
            map.put(node.getId(), node);
        }
        for (OrgTreeNode node : map.values()) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0L || !map.containsKey(parentId)) {
                roots.add(node);
            } else {
                map.get(parentId).getChildren().add(node);
            }
        }
        roots.sort(Comparator.comparing(OrgTreeNode::getSort).thenComparing(OrgTreeNode::getId));
        return roots;
    }

    @Override
    @Transactional
    public void create(OrgCreateRequest request) {
        Long parentId = request.getParentId() == null ? 0L : request.getParentId();
        SysOrg parent = null;
        if (parentId != 0L) {
            parent = orgMapper.selectById(parentId);
            if (parent == null) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "parent org not found");
            }
        }
        if (StringUtils.hasText(request.getCode())) {
            checkCodeUnique(request.getCode().trim(), null);
        }

        SysOrg entity = new SysOrg();
        entity.setParentId(parentId);
        entity.setName(request.getName().trim());
        entity.setCode(StringUtils.hasText(request.getCode()) ? request.getCode().trim() : null);
        entity.setLeaderUserId(request.getLeaderUserId());
        entity.setSort(request.getSort() == null ? 0 : request.getSort());
        entity.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        entity.setAncestors(parent == null ? "0" : parent.getAncestors() + "," + parent.getId());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        orgMapper.insert(entity);
    }

    @Override
    @Transactional
    public void update(Long id, OrgUpdateRequest request) {
        SysOrg entity = requireOrg(id);
        if (id == 1L && !Objects.equals(request.getParentId(), 0L)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "root org parent must be 0");
        }

        Long newParentId = request.getParentId() == null ? 0L : request.getParentId();
        if (Objects.equals(newParentId, id)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "org parent cannot be self");
        }
        if (StringUtils.hasText(request.getCode())) {
            checkCodeUnique(request.getCode().trim(), id);
        }

        String oldBranchPrefix = entity.getAncestors() + "," + entity.getId();
        String newAncestors;
        if (newParentId == 0L) {
            newAncestors = "0";
        } else {
            SysOrg parent = requireOrg(newParentId);
            String parentBranch = parent.getAncestors() + "," + parent.getId();
            if (parentBranch.startsWith(oldBranchPrefix)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "cannot move org under its descendant");
            }
            newAncestors = parent.getAncestors() + "," + parent.getId();
        }

        entity.setParentId(newParentId);
        entity.setName(request.getName().trim());
        entity.setCode(StringUtils.hasText(request.getCode()) ? request.getCode().trim() : null);
        entity.setLeaderUserId(request.getLeaderUserId());
        entity.setSort(request.getSort() == null ? entity.getSort() : request.getSort());
        entity.setAncestors(newAncestors);
        entity.setUpdateTime(LocalDateTime.now());
        orgMapper.updateById(entity);

        String newBranchPrefix = entity.getAncestors() + "," + entity.getId();
        List<SysOrg> descendants = orgMapper.selectList(
                new LambdaQueryWrapper<SysOrg>()
                        .ne(SysOrg::getId, id)
                        .apply("find_in_set({0}, ancestors)", id)
        );
        for (SysOrg row : descendants) {
            if (row.getAncestors() != null && row.getAncestors().startsWith(oldBranchPrefix)) {
                row.setAncestors(row.getAncestors().replaceFirst(oldBranchPrefix, newBranchPrefix));
                row.setUpdateTime(LocalDateTime.now());
                orgMapper.updateById(row);
            }
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysOrg entity = requireOrg(id);
        entity.setStatus(status == null ? 1 : status);
        entity.setUpdateTime(LocalDateTime.now());
        orgMapper.updateById(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == 1L) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "root org cannot be deleted");
        }
        SysOrg entity = requireOrg(id);
        Long childCount = orgMapper.selectCount(new LambdaQueryWrapper<SysOrg>().eq(SysOrg::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "org has child nodes");
        }

        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getOrgId, id));
        if (userCount != null && userCount > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "org still has users");
        }

        orgMapper.deleteById(entity.getId());
    }

    private void checkCodeUnique(String code, Long ignoreId) {
        LambdaQueryWrapper<SysOrg> wrapper = new LambdaQueryWrapper<SysOrg>().eq(SysOrg::getCode, code);
        if (ignoreId != null) {
            wrapper.ne(SysOrg::getId, ignoreId);
        }
        SysOrg existing = orgMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(HttpStatus.CONFLICT, "org code already exists");
        }
    }

    private SysOrg requireOrg(Long id) {
        SysOrg org = orgMapper.selectById(id);
        if (org == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "org not found");
        }
        return org;
    }

    private OrgTreeNode toNode(SysOrg row) {
        OrgTreeNode node = new OrgTreeNode();
        node.setId(row.getId());
        node.setParentId(row.getParentId());
        node.setName(row.getName());
        node.setCode(row.getCode());
        node.setLeaderUserId(row.getLeaderUserId());
        node.setStatus(row.getStatus());
        node.setSort(row.getSort());
        node.setAncestors(row.getAncestors());
        node.setCreateTime(row.getCreateTime());
        node.setUpdateTime(row.getUpdateTime());
        return node;
    }
}
