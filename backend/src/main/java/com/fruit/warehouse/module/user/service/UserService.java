package com.fruit.warehouse.module.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.module.user.entity.User;

public interface UserService {
    Page<User> pageUsers(long current, long size, String keyword);
    User getByUsername(String username);
    void create(User user);
    void update(User user);
    void delete(Long id);
    void upgradePassword(Long id, String encodedPassword);
}
