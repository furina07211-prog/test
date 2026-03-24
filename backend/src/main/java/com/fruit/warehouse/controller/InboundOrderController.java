package com.fruit.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.dto.InboundOrderCreateDTO;
import com.fruit.warehouse.dto.InboundReceiveDTO;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.InboundOrderService;
import com.fruit.warehouse.vo.InboundOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Api(tags = "入库管理")
@RestController
@RequestMapping("/api/inbound-orders")
@RequiredArgsConstructor
public class InboundOrderController {

    private final InboundOrderService inboundOrderService;

    @ApiOperation("分页查询入库单")
    @GetMapping
    @RequirePermission({"inbound:list"})
    public Result<Page<InboundOrderVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Long warehouseId) {
        return Result.success(inboundOrderService.page(pageNum, pageSize, orderCode, status, supplierId, warehouseId));
    }

    @ApiOperation("获取入库单详情")
    @GetMapping("/{id}")
    @RequirePermission({"inbound:list"})
    public Result<InboundOrderVO> getById(@PathVariable Long id) {
        return Result.success(inboundOrderService.getById(id));
    }

    @ApiOperation("创建入库单")
    @PostMapping
    @RequirePermission({"inbound:create"})
    public Result<Long> create(@Valid @RequestBody InboundOrderCreateDTO dto) {
        return Result.success(inboundOrderService.create(dto));
    }

    @ApiOperation("提交入库单")
    @PutMapping("/{id}/submit")
    @RequirePermission({"inbound:create"})
    public Result<Void> submit(@PathVariable Long id) {
        inboundOrderService.submit(id);
        return Result.success();
    }

    @ApiOperation("审核入库单")
    @PutMapping("/{id}/review")
    @RequirePermission({"inbound:review"})
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        boolean approved = (Boolean) params.getOrDefault("approved", true);
        String remark = (String) params.get("remark");
        inboundOrderService.review(id, approved, remark);
        return Result.success();
    }

    @ApiOperation("入库收货")
    @PostMapping("/{id}/receive")
    @RequirePermission({"inbound:receive"})
    public Result<Void> receive(@PathVariable Long id, @Valid @RequestBody InboundReceiveDTO dto) {
        dto.setOrderId(id);
        inboundOrderService.receive(dto);
        return Result.success();
    }

    @ApiOperation("完成入库单")
    @PutMapping("/{id}/complete")
    @RequirePermission({"inbound:receive"})
    public Result<Void> complete(@PathVariable Long id) {
        inboundOrderService.complete(id);
        return Result.success();
    }

    @ApiOperation("取消入库单")
    @PutMapping("/{id}/cancel")
    @RequirePermission({"inbound:update"})
    public Result<Void> cancel(@PathVariable Long id) {
        inboundOrderService.cancel(id);
        return Result.success();
    }
}
