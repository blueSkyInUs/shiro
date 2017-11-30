package com.mybatis;

import com.init.domain.Roles;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserRoleMapper {

    @Select("select * from user_roles where username = #{username}")
    List<Roles> obtainRoles(String username);
}
