package com.fruit.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.entity.SysPermission;
import com.fruit.warehouse.entity.SysRole;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "角色管理")
@RestController
@RequestMapping("/api/sys/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @ApiOperation("分页查询角色")
    @GetMapping
    @RequirePermission({"role:list"})
    public Result<Page<SysRole>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName) {
        return Result.success(roleService.page(pageNum, pageSize, roleName));
    }

    @ApiOperation("获取所有角色")
    @GetMapping("/all")
    public Result<List<SysRole>> listAll() {
        return Result.success(roleService.listAll());
    }

    @ApiOperation("获取角色详情")
    @GetMapping("/{id}")
    @RequirePermission({"role:list"})
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @ApiOperation("创建角色")
    @PostMapping
    @RequirePermission({"role:create"})
    public Result<Void> create(@RequestBody SysRole role) {
        roleService.create(role);
        return Result.success();
    }

    @ApiOperation("更新角色")
    @PutMapping("/{id}")
    @RequirePermission({"role:update"})
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        roleService.update(role);
        return Result.success();
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    @RequirePermission({"role:delete"})
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @ApiOperation("获取角色权限")
    @GetMapping("/{id}/permissions")
    @RequirePermission({"role:list"})
    public Result<List<SysPermission>> getPermissions(@PathVariable Long id) {
        return Result.success(roleService.getPermissions(id));
    }

    @ApiOperation("分配角色权限")
    @PutMapping("/{id}/permissions")
    @RequirePermission({"role:update"})
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> params) {
        roleService.assignPermissions(id, params.get("permissionIds"));
        return Result.success();
    }

    @ApiOperation("获取所有权限")
    @GetMapping("/permissions")
    public Result<List<SysPermission>> listAllPermissions() {
        return Result.success(roleService.listAllPermissions());
    }
}
