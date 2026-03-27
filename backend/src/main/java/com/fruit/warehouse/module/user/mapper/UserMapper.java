package com.fruit.warehouse.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.warehouse.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("""
        SELECT u.id,
               u.username,
               u.password,
               u.real_name,
               u.phone,
               u.email,
               u.status,
               u.create_time,
               u.update_time,
               r.role_code AS role_code
        FROM sys_user u
        LEFT JOIN sys_user_role ur ON ur.user_id = u.id
        LEFT JOIN sys_role r ON r.id = ur.role_id
        WHERE u.username = #{username}
        LIMIT 1
        """)
    User selectByUsernameWithRole(@Param("username") String username);
}
