package com.fruit.warehouse.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.BusinessException;
import com.fruit.warehouse.dto.UserCreateDTO;
import com.fruit.warehouse.dto.UserUpdateDTO;
import com.fruit.warehouse.entity.SysRole;
import com.fruit.warehouse.entity.SysUser;
import com.fruit.warehouse.entity.SysUserRole;
import com.fruit.warehouse.mapper.SysRoleMapper;
import com.fruit.warehouse.mapper.SysUserMapper;
import com.fruit.warehouse.mapper.SysUserRoleMapper;
import com.fruit.warehouse.security.PasswordUtils;
import com.fruit.warehouse.service.SysUserService;
import com.fruit.warehouse.vo.RoleVO;
import com.fruit.warehouse.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    @Override
    public Page<UserVO> page(Integer pageNum, Integer pageSize, String username, String realName, Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(username), SysUser::getUsername, username)
               .like(StrUtil.isNotBlank(realName), SysUser::getRealName, realName)
               .eq(status != null, SysUser::getStatus, status)
               .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = userMapper.selectPage(page, wrapper);

        Page<UserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public UserVO getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user);
    }

    @Override
    @Transactional
    public void create(UserCreateDTO dto) {
        // Check username uniqueness
        SysUser existing = userMapper.selectByUsername(dto.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        BeanUtil.copyProperties(dto, user);
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);

        // Assign roles
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            for (Long roleId : dto.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                ur.setCreateTime(new Date());
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    @Transactional
    public void update(UserUpdateDTO dto) {
        SysUser user = userMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        BeanUtil.copyProperties(dto, user, "id", "username", "password");
        userMapper.updateById(user);

        // Update roles
        if (dto.getRoleIds() != null) {
            userRoleMapper.deleteByUserId(user.getId());
            for (Long roleId : dto.getRoleIds()) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(user.getId());
                ur.setRoleId(roleId);
                ur.setCreateTime(new Date());
                userRoleMapper.insert(ur);
            }
        }
    }

    @Override
    public void delete(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除管理员账号");
        }
        userMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(PasswordUtils.encode(newPassword));
        userMapper.updateById(user);
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        vo.setRoles(roles.stream().map(r -> {
            RoleVO rv = new RoleVO();
            BeanUtil.copyProperties(r, rv);
            return rv;
        }).collect(Collectors.toList()));
        return vo;
    }
}
