package com.fruit.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.dto.UserCreateDTO;
import com.fruit.warehouse.dto.UserUpdateDTO;
import com.fruit.warehouse.entity.SysUser;
import com.fruit.warehouse.vo.UserVO;

public interface SysUserService {

    Page<UserVO> page(Integer pageNum, Integer pageSize, String username, String realName, Integer status);

    UserVO getById(Long id);

    void create(UserCreateDTO dto);

    void update(UserUpdateDTO dto);

    void delete(Long id);

    void updateStatus(Long id, Integer status);

    void resetPassword(Long id, String newPassword);
}
