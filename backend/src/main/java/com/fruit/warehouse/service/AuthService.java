package com.fruit.warehouse.service;

import com.fruit.warehouse.dto.LoginDTO;
import com.fruit.warehouse.vo.LoginVO;
import com.fruit.warehouse.vo.UserVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    void logout();

    UserVO getCurrentUser();

    void changePassword(String oldPassword, String newPassword);
}
