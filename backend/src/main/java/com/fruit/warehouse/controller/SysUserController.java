package com.fruit.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.dto.UserCreateDTO;
import com.fruit.warehouse.dto.UserUpdateDTO;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.SysUserService;
import com.fruit.warehouse.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/sys/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @ApiOperation("分页查询用户")
    @GetMapping
    @RequirePermission({"user:list"})
    public Result<Page<UserVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.page(pageNum, pageSize, username, realName, status));
    }

    @ApiOperation("获取用户详情")
    @GetMapping("/{id}")
    @RequirePermission({"user:list"})
    public Result<UserVO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @ApiOperation("创建用户")
    @PostMapping
    @RequirePermission({"user:create"})
    public Result<Void> create(@Valid @RequestBody UserCreateDTO dto) {
        userService.create(dto);
        return Result.success();
    }

    @ApiOperation("更新用户")
    @PutMapping("/{id}")
    @RequirePermission({"user:update"})
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        dto.setId(id);
        userService.update(dto);
        return Result.success();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    @RequirePermission({"user:delete"})
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @ApiOperation("更新用户状态")
    @PutMapping("/{id}/status")
    @RequirePermission({"user:update"})
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        userService.updateStatus(id, params.get("status"));
        return Result.success();
    }

    @ApiOperation("重置密码")
    @PutMapping("/{id}/reset-password")
    @RequirePermission({"user:update"})
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> params) {
        userService.resetPassword(id, params.getOrDefault("password", "123456"));
        return Result.success();
    }
}
