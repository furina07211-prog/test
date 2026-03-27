package com.fruit.warehouse.module.purchase.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.module.purchase.dto.PurchaseOrderCreateRequest;
import com.fruit.warehouse.module.purchase.dto.PurchaseOrderPageQuery;
import com.fruit.warehouse.module.purchase.dto.PurchaseReceiveRequest;
import com.fruit.warehouse.module.purchase.entity.PurchaseOrder;
import com.fruit.warehouse.module.purchase.service.PurchaseService;
import com.fruit.warehouse.module.purchase.vo.PurchaseOrderItemVO;
import com.fruit.warehouse.module.purchase.vo.PurchaseOrderPageVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase/orders")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<PurchaseOrder> create(@RequestBody PurchaseOrderCreateRequest request) {
        return Result.success(purchaseService.createOrder(request));
    }

    @GetMapping
    public Result<IPage<PurchaseOrderPageVO>> page(@RequestParam(defaultValue = "1") Integer pageNo,
                                                    @RequestParam(defaultValue = "10") Integer pageSize,
                                                    @RequestParam(required = false) String purchaseNo,
                                                    @RequestParam(required = false) String status,
                                                    @RequestParam(required = false) Long supplierId,
                                                    @RequestParam(required = false) Long warehouseId) {
        PurchaseOrderPageQuery query = new PurchaseOrderPageQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setPurchaseNo(purchaseNo);
        query.setStatus(status);
        query.setSupplierId(supplierId);
        query.setWarehouseId(warehouseId);
        return Result.success(purchaseService.pageList(query));
    }

    @GetMapping("/{id}/items")
    public Result<List<PurchaseOrderItemVO>> items(@PathVariable Long id) {
        return Result.success(purchaseService.listItems(id));
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
