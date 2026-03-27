package com.fruit.warehouse.module.auth.controller;

import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.common.result.Results;
import com.fruit.warehouse.common.util.JwtUtils;
import com.fruit.warehouse.module.auth.dto.LoginRequest;
import com.fruit.warehouse.module.auth.dto.LoginResponse;
import com.fruit.warehouse.module.user.entity.User;
import com.fruit.warehouse.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.getByUsername(request.getUsername());
        if (user == null || !isPasswordValid(user, request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!StringUtils.hasText(user.getRoleCode())) {
            throw new BusinessException("用户未绑定角色，请联系管理员");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleCode());
        user.setPassword(null);
        LoginResponse resp = LoginResponse.builder()
            .token(token)
            .user(user)
            .roles(List.of(user.getRoleCode()))
            .build();
        return Results.ok(resp);
    }

    @GetMapping("/me")
    public Result<LoginResponse> me(Authentication authentication) {
        if (authentication == null) {
            throw new BusinessException("未登录");
        }
        User user = userService.getByUsername(authentication.getName());
        if (user != null) {
            user.setPassword(null);
        }
        List<String> roles = (user != null && StringUtils.hasText(user.getRoleCode()))
            ? List.of(user.getRoleCode())
            : List.of();

        LoginResponse resp = LoginResponse.builder()
            .user(user)
            .roles(roles)
            .build();
        return Results.ok(resp);
    }

    private boolean isPasswordValid(User user, String rawPassword) {
        String dbPassword = user.getPassword();
        if (!StringUtils.hasText(dbPassword)) {
            return false;
        }

        if (passwordEncoder.matches(rawPassword, dbPassword)) {
            return true;
        }

        // Compatibility for old seed data with plaintext passwords, then auto-upgrade to BCrypt.
        if (rawPassword.equals(dbPassword)) {
            String encoded = passwordEncoder.encode(rawPassword);
            userService.upgradePassword(user.getId(), encoded);
            user.setPassword(encoded);
            return true;
        }
        return false;
    }
}
