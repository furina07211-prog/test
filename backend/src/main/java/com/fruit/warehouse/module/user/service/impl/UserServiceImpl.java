package com.fruit.warehouse.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.user.entity.User;
import com.fruit.warehouse.module.user.mapper.UserMapper;
import com.fruit.warehouse.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> pageUsers(long current, long size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(keyword), User::getUsername, keyword)
            .or(StringUtils.isNotBlank(keyword))
            .like(StringUtils.isNotBlank(keyword), User::getRealName, keyword)
            .orderByDesc(User::getCreateTime);
        Page<User> page = userMapper.selectPage(new Page<>(current, size), wrapper);
        page.getRecords().forEach(u -> u.setPassword(null));
        return page;
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsernameWithRole(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(User user) {
        if (getByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User user) {
        User db = userMapper.selectById(user.getId());
        if (db == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.isBlank(user.getPassword())) {
            user.setPassword(db.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upgradePassword(Long id, String encodedPassword) {
        User update = new User();
        update.setId(id);
        update.setPassword(encodedPassword);
        userMapper.updateById(update);
    }
}
