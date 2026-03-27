package com.fruit.warehouse.module.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.module.sales.dto.SalesOrderCreateRequest;
import com.fruit.warehouse.module.sales.dto.SalesOrderPageQuery;
import com.fruit.warehouse.module.sales.dto.SalesShipRequest;
import com.fruit.warehouse.module.sales.entity.SalesOrder;
import com.fruit.warehouse.module.sales.service.SalesService;
import com.fruit.warehouse.module.sales.vo.SalesOrderItemVO;
import com.fruit.warehouse.module.sales.vo.SalesOrderPageVO;
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
@RequestMapping("/api/sales/orders")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public Result<SalesOrder> create(@RequestBody SalesOrderCreateRequest request) {
        return Result.success(salesService.createOrder(request));
    }

    @GetMapping
    public Result<IPage<SalesOrderPageVO>> page(@RequestParam(defaultValue = "1") Integer pageNo,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) String salesNo,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) Long customerId,
                                                @RequestParam(required = false) Long warehouseId) {
        SalesOrderPageQuery query = new SalesOrderPageQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setSalesNo(salesNo);
        query.setStatus(status);
        query.setCustomerId(customerId);
        query.setWarehouseId(warehouseId);
        return Result.success(salesService.pageList(query));
    }

    @GetMapping("/{id}/items")
    public Result<List<SalesOrderItemVO>> items(@PathVariable Long id) {
        return Result.success(salesService.listItems(id));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public Result<SalesOrder> submit(@PathVariable Long id) {
        return Result.success(salesService.submit(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<SalesOrder> approve(@PathVariable Long id) {
        return Result.success(salesService.approve(id));
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<SalesOrder> ship(@PathVariable Long id, @RequestBody SalesShipRequest request) {
        return Result.success(salesService.ship(id, request));
    }
}
