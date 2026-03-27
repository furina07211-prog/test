package com.fruit.warehouse.module.inventory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.common.result.Results;
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

/**
 * 库存管理 模块控制器。
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryAlertService alertService;
    private final StockCheckService stockCheckService;
    private final StockCheckItemMapper stockCheckItemMapper;

    /**
     * 分页查询库存批次。
     */
    @GetMapping("/batches")
    public Result<IPage<InventoryBatch>> page(InventoryQueryRequest request) {
        return Results.ok(inventoryService.pageQuery(request));
    }

    /**
     * 新增库存批次。
     */
    @PostMapping("/batches")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<InventoryBatch> create(@RequestBody InventoryBatch batch) {
        inventoryService.save(batch);
        return Results.ok(batch);
    }

    /**
     * 更新库存批次信息。
     */
    @PutMapping("/batches/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody InventoryBatch batch) {
        batch.setId(id);
        return Results.ok(inventoryService.updateById(batch));
    }

    /**
     * 删除库存批次。
     */
    @DeleteMapping("/batches/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Results.ok(inventoryService.removeById(id));
    }

    /**
     * 查询低于安全库存的批次。
     */
    @GetMapping("/batches/low-safety")
    public Result<List<InventoryBatch>> lowSafety() {
        return Results.ok(inventoryService.listLowStock());
    }

    /**
     * 查询临期批次。
     */
    @GetMapping("/batches/near-expire")
    public Result<List<InventoryBatch>> nearExpire(@RequestParam(defaultValue = "7") int warningDays) {
        return Results.ok(inventoryService.listNearExpire(warningDays));
    }

    /**
     * 查询未处理库存预警。
     */
    @GetMapping("/alerts")
    public Result<List<InventoryAlert>> alerts() {
        return Results.ok(alertService.listActive());
    }

    /**
     * 处理指定预警记录。
     */
    @PostMapping("/alerts/{id}/handle")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Void> handleAlert(@PathVariable Long id, @RequestParam(required = false) Long handlerId) {
        alertService.handleAlert(id, handlerId);
        return Results.ok();
    }

    /**
     * 新建盘点单。
     */
    @PostMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<StockCheckOrder> createCheck(@RequestBody StockCheckCreateRequest request) {
        return Results.ok(stockCheckService.createOrder(request));
    }

    /**
     * 分页查询盘点单。
     */
    @GetMapping("/check")
    public Result<IPage<StockCheckOrder>> listChecks(@RequestParam(defaultValue = "1") int pageNo,
                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                     @RequestParam(required = false) Long warehouseId,
                                                     @RequestParam(required = false) String status) {
        return Results.ok(stockCheckService.pageList(pageNo, pageSize, warehouseId, status));
    }

    /**
     * 查询盘点单明细行。
     */
    @GetMapping("/check/{id}/items")
    public Result<List<StockCheckItem>> checkItems(@PathVariable Long id) {
        return Results.ok(stockCheckItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockCheckItem>()
                        .eq(StockCheckItem::getCheckOrderId, id)));
    }

    /**
     * 审核盘点单并执行库存调整。
     */
    @PostMapping("/check/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<StockCheckOrder> approve(@PathVariable Long id, @RequestBody StockCheckApproveRequest request) {
        return Results.ok(stockCheckService.approve(id, request));
    }
}
