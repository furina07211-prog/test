package com.fruit.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.entity.Warehouse;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.WarehouseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "仓库管理")
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @ApiOperation("分页查询仓库")
    @GetMapping
    @RequirePermission({"warehouse:list"})
    public Result<Page<Warehouse>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String warehouseName) {
        return Result.success(warehouseService.page(pageNum, pageSize, warehouseName));
    }

    @ApiOperation("获取所有仓库")
    @GetMapping("/all")
    public Result<List<Warehouse>> listAll() {
        return Result.success(warehouseService.listAll());
    }

    @ApiOperation("获取仓库详情")
    @GetMapping("/{id}")
    @RequirePermission({"warehouse:list"})
    public Result<Warehouse> getById(@PathVariable Long id) {
        return Result.success(warehouseService.getById(id));
    }

    @ApiOperation("创建仓库")
    @PostMapping
    @RequirePermission({"warehouse:create"})
    public Result<Void> create(@RequestBody Warehouse warehouse) {
        warehouseService.create(warehouse);
        return Result.success();
    }

    @ApiOperation("更新仓库")
    @PutMapping("/{id}")
    @RequirePermission({"warehouse:update"})
    public Result<Void> update(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        warehouse.setId(id);
        warehouseService.update(warehouse);
        return Result.success();
    }

    @ApiOperation("删除仓库")
    @DeleteMapping("/{id}")
    @RequirePermission({"warehouse:delete"})
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.delete(id);
        return Result.success();
    }
}
