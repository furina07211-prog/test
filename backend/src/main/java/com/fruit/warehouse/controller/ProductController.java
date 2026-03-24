package com.fruit.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.entity.Product;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品管理")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @ApiOperation("分页查询商品")
    @GetMapping
    @RequirePermission({"product:list"})
    public Result<Page<Product>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.success(productService.page(pageNum, pageSize, productName, categoryId, status));
    }

    @ApiOperation("获取所有商品")
    @GetMapping("/all")
    public Result<List<Product>> listAll() {
        return Result.success(productService.listAll());
    }

    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    @RequirePermission({"product:list"})
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @ApiOperation("创建商品")
    @PostMapping
    @RequirePermission({"product:create"})
    public Result<Void> create(@RequestBody Product product) {
        productService.create(product);
        return Result.success();
    }

    @ApiOperation("更新商品")
    @PutMapping("/{id}")
    @RequirePermission({"product:update"})
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productService.update(product);
        return Result.success();
    }

    @ApiOperation("删除商品")
    @DeleteMapping("/{id}")
    @RequirePermission({"product:delete"})
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
}
