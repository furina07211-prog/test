package com.fruit.warehouse.module.purchase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.module.purchase.dto.PurchaseOrderCreateRequest;
import com.fruit.warehouse.module.purchase.dto.PurchaseReceiveRequest;
import com.fruit.warehouse.module.purchase.entity.PurchaseOrder;
import com.fruit.warehouse.module.purchase.entity.PurchaseOrderItem;
import com.fruit.warehouse.module.purchase.mapper.PurchaseOrderItemMapper;
import com.fruit.warehouse.module.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/purchase/orders")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final PurchaseOrderItemMapper itemMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<PurchaseOrder> create(@RequestBody PurchaseOrderCreateRequest request) {
        return Result.success(purchaseService.createOrder(request));
    }

    @GetMapping
    public Result<IPage<PurchaseOrder>> page(@RequestParam(defaultValue = "1") int pageNo,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) Long supplierId) {
        return Result.success(purchaseService.pageList(pageNo, pageSize, status, supplierId));
    }

    @GetMapping("/{id}/items")
    public Result<List<PurchaseOrderItem>> items(@PathVariable Long id) {
        return Result.success(itemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PurchaseOrderItem>()
                        .eq(PurchaseOrderItem::getPurchaseOrderId, id)));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<PurchaseOrder> submit(@PathVariable Long id) {
        return Result.success(purchaseService.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<PurchaseOrder> approve(@PathVariable Long id) {
        return Result.success(purchaseService.approve(id));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<PurchaseOrder> receive(@PathVariable Long id, @RequestBody PurchaseReceiveRequest request) {
        return Result.success(purchaseService.receive(id, request));
    }
}
