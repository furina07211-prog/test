package com.fruit.warehouse.module.inventory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.module.inventory.dto.InventoryQueryRequest;
import com.fruit.warehouse.module.inventory.dto.StockCheckApproveRequest;
import com.fruit.warehouse.module.inventory.dto.StockCheckCreateRequest;
import com.fruit.warehouse.module.inventory.entity.InventoryAlert;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import com.fruit.warehouse.module.inventory.entity.StockCheckItem;
import com.fruit.warehouse.module.inventory.entity.StockCheckOrder;
import com.fruit.warehouse.module.inventory.mapper.StockCheckItemMapper;
import com.fruit.warehouse.module.inventory.service.InventoryAlertService;
import com.fruit.warehouse.module.inventory.service.InventoryService;
import com.fruit.warehouse.module.inventory.service.StockCheckService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryAlertService alertService;
    private final StockCheckService stockCheckService;
    private final StockCheckItemMapper stockCheckItemMapper;

    @GetMapping("/batches")
    public Result<IPage<InventoryBatch>> page(InventoryQueryRequest request) {
        return Result.success(inventoryService.pageQuery(request));
    }

    @PostMapping("/batches")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<InventoryBatch> create(@RequestBody InventoryBatch batch) {
        inventoryService.save(batch);
        return Result.success(batch);
    }

    @PutMapping("/batches/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody InventoryBatch batch) {
        batch.setId(id);
        return Result.success(inventoryService.updateById(batch));
    }

    @DeleteMapping("/batches/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(inventoryService.removeById(id));
    }

    @GetMapping("/batches/low-safety")
    public Result<List<InventoryBatch>> lowSafety() {
        return Result.success(inventoryService.listLowStock());
    }

    @GetMapping("/batches/near-expire")
    public Result<List<InventoryBatch>> nearExpire(@RequestParam(defaultValue = "7") int warningDays) {
        return Result.success(inventoryService.listNearExpire(warningDays));
    }

    @GetMapping("/alerts")
    public Result<List<InventoryAlert>> alerts() {
        return Result.success(alertService.listActive());
    }

    @PostMapping("/alerts/{id}/handle")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Void> handleAlert(@PathVariable Long id, @RequestParam(required = false) Long handlerId) {
        alertService.handleAlert(id, handlerId);
        return Result.success();
    }

    @PostMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<StockCheckOrder> createCheck(@RequestBody StockCheckCreateRequest request) {
        return Result.success(stockCheckService.createOrder(request));
    }

    @GetMapping("/check")
    public Result<IPage<StockCheckOrder>> listChecks(@RequestParam(defaultValue = "1") int pageNo,
                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                     @RequestParam(required = false) Long warehouseId,
                                                     @RequestParam(required = false) String status) {
        return Result.success(stockCheckService.pageList(pageNo, pageSize, warehouseId, status));
    }

    @GetMapping("/check/{id}/items")
    public Result<List<StockCheckItem>> checkItems(@PathVariable Long id) {
        return Result.success(stockCheckItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockCheckItem>()
                        .eq(StockCheckItem::getCheckOrderId, id)));
    }

    @PostMapping("/check/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<StockCheckOrder> approve(@PathVariable Long id, @RequestBody StockCheckApproveRequest request) {
        return Result.success(stockCheckService.approve(id, request));
    }
}
