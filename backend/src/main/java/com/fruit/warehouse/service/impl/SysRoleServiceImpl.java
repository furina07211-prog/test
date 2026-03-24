package com.fruit.warehouse.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.BusinessException;
import com.fruit.warehouse.entity.SysPermission;
import com.fruit.warehouse.entity.SysRole;
import com.fruit.warehouse.entity.SysRolePermission;
import com.fruit.warehouse.mapper.SysPermissionMapper;
import com.fruit.warehouse.mapper.SysRoleMapper;
import com.fruit.warehouse.mapper.SysRolePermissionMapper;
import com.fruit.warehouse.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;

    @Override
    public Page<SysRole> page(Integer pageNum, Integer pageSize, String roleName) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(roleName), SysRole::getRoleName, roleName)
               .orderByAsc(SysRole::getId);
        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getId));
    }

    @Override
    public SysRole getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public void create(SysRole role) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, role.getRoleCode());
        if (roleMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }
        role.setStatus(1);
        roleMapper.insert(role);
    }

    @Override
    public void update(SysRole role) {
        SysRole existing = roleMapper.selectById(role.getId());
        if (existing == null) {
            throw new BusinessException("角色不存在");
        }
        roleMapper.updateById(role);
    }

    @Override
    public void delete(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        if ("ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException("不能删除管理员角色");
        }
        roleMapper.deleteById(id);
    }

    @Override
    public List<SysPermission> getPermissions(Long roleId) {
        return permissionMapper.selectPermissionsByRoleId(roleId);
    }

    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permId : permissionIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permId);
                rp.setCreateTime(new Date());
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public List<SysPermission> listAllPermissions() {
        return permissionMapper.selectList(new LambdaQueryWrapper<>());
    }
}
