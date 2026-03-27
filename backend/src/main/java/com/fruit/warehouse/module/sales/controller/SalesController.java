package com.fruit.warehouse.module.sales.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.module.sales.dto.SalesOrderCreateRequest;
import com.fruit.warehouse.module.sales.dto.SalesShipRequest;
import com.fruit.warehouse.module.sales.entity.SalesOrder;
import com.fruit.warehouse.module.sales.entity.SalesOrderItem;
import com.fruit.warehouse.module.sales.mapper.SalesOrderItemMapper;
import com.fruit.warehouse.module.sales.service.SalesService;
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
    private final SalesOrderItemMapper itemMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public Result<SalesOrder> create(@RequestBody SalesOrderCreateRequest request) {
        return Result.success(salesService.createOrder(request));
    }

    @GetMapping
    public Result<IPage<SalesOrder>> page(@RequestParam(defaultValue = "1") int pageNo,
                                          @RequestParam(defaultValue = "10") int pageSize,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) Long customerId) {
        return Result.success(salesService.pageList(pageNo, pageSize, status, customerId));
    }

    @GetMapping("/{id}/items")
    public Result<List<SalesOrderItem>> items(@PathVariable Long id) {
        return Result.success(itemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SalesOrderItem>()
                        .eq(SalesOrderItem::getSalesOrderId, id)));
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
