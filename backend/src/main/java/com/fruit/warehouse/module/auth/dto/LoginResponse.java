package com.fruit.warehouse.module.auth.dto;

import com.fruit.warehouse.module.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {
    private String token;
    private User user;
    private List<String> roles;
}
