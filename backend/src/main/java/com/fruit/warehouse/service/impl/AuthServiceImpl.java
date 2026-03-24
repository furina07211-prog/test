package com.fruit.warehouse.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.fruit.warehouse.common.BusinessException;
import com.fruit.warehouse.dto.LoginDTO;
import com.fruit.warehouse.entity.SysRole;
import com.fruit.warehouse.entity.SysUser;
import com.fruit.warehouse.mapper.SysPermissionMapper;
import com.fruit.warehouse.mapper.SysRoleMapper;
import com.fruit.warehouse.mapper.SysUserMapper;
import com.fruit.warehouse.security.JwtUtils;
import com.fruit.warehouse.security.PasswordUtils;
import com.fruit.warehouse.security.UserContext;
import com.fruit.warehouse.service.AuthService;
import com.fruit.warehouse.vo.LoginVO;
import com.fruit.warehouse.vo.RoleVO;
import com.fruit.warehouse.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final JwtUtils jwtUtils;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }
        if (!PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // Update last login time
        user.setLastLoginTime(new Date());
        userMapper.updateById(user);

        // Get roles
        List<SysRole> roles = roleMapper.selectRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());

        // Get permissions
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(user.getId());

        // Generate token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), roleCodes);

        // Build response
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setPermissions(permissions);

        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        userVO.setRoles(roles.stream().map(r -> {
            RoleVO rv = new RoleVO();
            BeanUtil.copyProperties(r, rv);
            return rv;
        }).collect(Collectors.toList()));
        vo.setUserInfo(userVO);

        return vo;
    }

    @Override
    public void logout() {
        // JWT is stateless, client just discards the token
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);

        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        vo.setRoles(roles.stream().map(r -> {
            RoleVO rv = new RoleVO();
            BeanUtil.copyProperties(r, rv);
            return rv;
        }).collect(Collectors.toList()));

        return vo;
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = UserContext.getUserId();
        SysUser user = userMapper.selectById(userId);

        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(PasswordUtils.encode(newPassword));
        userMapper.updateById(user);
    }
}
