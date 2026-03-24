package com.fruit.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.entity.Supplier;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.SupplierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "供应商管理")
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @ApiOperation("分页查询供应商")
    @GetMapping
    @RequirePermission({"supplier:list"})
    public Result<Page<Supplier>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String supplierName) {
        return Result.success(supplierService.page(pageNum, pageSize, supplierName));
    }

    @ApiOperation("获取所有供应商")
    @GetMapping("/all")
    public Result<List<Supplier>> listAll() {
        return Result.success(supplierService.listAll());
    }

    @ApiOperation("获取供应商详情")
    @GetMapping("/{id}")
    @RequirePermission({"supplier:list"})
    public Result<Supplier> getById(@PathVariable Long id) {
        return Result.success(supplierService.getById(id));
    }

    @ApiOperation("创建供应商")
    @PostMapping
    @RequirePermission({"supplier:create"})
    public Result<Void> create(@RequestBody Supplier supplier) {
        supplierService.create(supplier);
        return Result.success();
    }

    @ApiOperation("更新供应商")
    @PutMapping("/{id}")
    @RequirePermission({"supplier:update"})
    public Result<Void> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        supplierService.update(supplier);
        return Result.success();
    }

    @ApiOperation("删除供应商")
    @DeleteMapping("/{id}")
    @RequirePermission({"supplier:delete"})
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return Result.success();
    }
}
