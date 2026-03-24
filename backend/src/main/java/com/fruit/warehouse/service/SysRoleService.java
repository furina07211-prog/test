package com.fruit.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.entity.SysPermission;
import com.fruit.warehouse.entity.SysRole;

import java.util.List;

public interface SysRoleService {

    Page<SysRole> page(Integer pageNum, Integer pageSize, String roleName);

    List<SysRole> listAll();

    SysRole getById(Long id);

    void create(SysRole role);

    void update(SysRole role);

    void delete(Long id);

    List<SysPermission> getPermissions(Long roleId);

    void assignPermissions(Long roleId, List<Long> permissionIds);

    List<SysPermission> listAllPermissions();
}
