package com.modelhub.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.modelhub.backend.entity.SysRole;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {
    @Select("SELECT r.code " +
            "FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT ar.perm_code " +
            "FROM sys_api_resource ar " +
            "INNER JOIN sys_role_api ra ON ra.api_id = ar.id " +
            "INNER JOIN sys_role r ON r.id = ra.role_id " +
            "INNER JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} " +
            "AND r.status = 1 " +
            "AND ar.status = 1")
    List<String> selectApiPermCodesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT r.data_scope_type " +
            "FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectDataScopeTypesByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT rs.org_id " +
            "FROM sys_role_data_scope_org rs " +
            "INNER JOIN sys_user_role ur ON ur.role_id = rs.role_id " +
            "INNER JOIN sys_role r ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<Long> selectCustomScopeOrgIdsByUserId(@Param("userId") Long userId);
}
