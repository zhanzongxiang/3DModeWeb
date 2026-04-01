package com.modelhub.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.modelhub.backend.entity.SysApiResource;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysApiResourceMapper extends BaseMapper<SysApiResource> {
    @Select("SELECT perm_code FROM sys_api_resource WHERE status = 1")
    List<String> selectAllEnabledPermCodes();
}
