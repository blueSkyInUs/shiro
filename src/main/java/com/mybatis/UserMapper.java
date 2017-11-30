package com.mybatis;

import com.init.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select * from users where username = #{username}")
    List<User> obtainUsers(String username);
}
