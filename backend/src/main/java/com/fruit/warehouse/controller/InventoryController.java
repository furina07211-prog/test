package com.fruit.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.InventoryService;
import com.fruit.warehouse.vo.BatchVO;
import com.fruit.warehouse.vo.InventoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "库存管理")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @ApiOperation("分页查询库存")
    @GetMapping
    @RequirePermission({"inventory:list"})
    public Result<Page<InventoryVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(inventoryService.page(pageNum, pageSize, productName, warehouseId, categoryId));
    }

    @ApiOperation("分页查询批次")
    @GetMapping("/batches")
    @RequirePermission({"batch:list"})
    public Result<Page<BatchVO>> pageBatches(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer status) {
        return Result.success(inventoryService.pageBatches(pageNum, pageSize, productId, warehouseId, status));
    }

    @ApiOperation("获取商品在仓库的库存详情")
    @GetMapping("/{productId}/{warehouseId}")
    @RequirePermission({"inventory:list"})
    public Result<InventoryVO> getByProductAndWarehouse(
            @PathVariable Long productId,
            @PathVariable Long warehouseId) {
        return Result.success(inventoryService.getByProductAndWarehouse(productId, warehouseId));
    }
}
