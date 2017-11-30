package com.mybatis;

import com.init.domain.Permissions;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionsMapper {

    @Select("select * from roles_permissions where role_name = #{roleName}")
    List<Permissions> obtainPermissions(String roleName);
}
