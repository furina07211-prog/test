package com.fruit.warehouse.controller;

import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.entity.Category;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "分类管理")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @ApiOperation("获取所有分类")
    @GetMapping
    public Result<List<Category>> listAll() {
        return Result.success(categoryService.listAll());
    }

    @ApiOperation("获取分类详情")
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @ApiOperation("创建分类")
    @PostMapping
    @RequirePermission({"category:create"})
    public Result<Void> create(@RequestBody Category category) {
        categoryService.create(category);
        return Result.success();
    }

    @ApiOperation("更新分类")
    @PutMapping("/{id}")
    @RequirePermission({"category:update"})
    public Result<Void> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.update(category);
        return Result.success();
    }

    @ApiOperation("删除分类")
    @DeleteMapping("/{id}")
    @RequirePermission({"category:delete"})
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
