package com.fruit.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.dto.OutboundOrderCreateDTO;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.OutboundOrderService;
import com.fruit.warehouse.vo.OutboundOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@Api(tags = "出库管理")
@RestController
@RequestMapping("/api/outbound-orders")
@RequiredArgsConstructor
public class OutboundOrderController {

    private final OutboundOrderService outboundOrderService;

    @ApiOperation("分页查询出库单")
    @GetMapping
    @RequirePermission({"outbound:list"})
    public Result<Page<OutboundOrderVO>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long warehouseId) {
        return Result.success(outboundOrderService.page(pageNum, pageSize, orderCode, status, warehouseId));
    }

    @ApiOperation("获取出库单详情")
    @GetMapping("/{id}")
    @RequirePermission({"outbound:list"})
    public Result<OutboundOrderVO> getById(@PathVariable Long id) {
        return Result.success(outboundOrderService.getById(id));
    }

    @ApiOperation("创建出库单")
    @PostMapping
    @RequirePermission({"outbound:create"})
    public Result<Long> create(@Valid @RequestBody OutboundOrderCreateDTO dto) {
        return Result.success(outboundOrderService.create(dto));
    }

    @ApiOperation("提交出库单")
    @PutMapping("/{id}/submit")
    @RequirePermission({"outbound:create"})
    public Result<Void> submit(@PathVariable Long id) {
        outboundOrderService.submit(id);
        return Result.success();
    }

    @ApiOperation("审核出库单")
    @PutMapping("/{id}/review")
    @RequirePermission({"outbound:review"})
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        boolean approved = (Boolean) params.getOrDefault("approved", true);
        String remark = (String) params.get("remark");
        outboundOrderService.review(id, approved, remark);
        return Result.success();
    }

    @ApiOperation("出库拣货")
    @PostMapping("/{id}/pick")
    @RequirePermission({"outbound:pick"})
    public Result<Void> pick(@PathVariable Long id) {
        outboundOrderService.pick(id);
        return Result.success();
    }

    @ApiOperation("完成出库单")
    @PutMapping("/{id}/complete")
    @RequirePermission({"outbound:pick"})
    public Result<Void> complete(@PathVariable Long id) {
        outboundOrderService.complete(id);
        return Result.success();
    }

    @ApiOperation("取消出库单")
    @PutMapping("/{id}/cancel")
    @RequirePermission({"outbound:update"})
    public Result<Void> cancel(@PathVariable Long id) {
        outboundOrderService.cancel(id);
        return Result.success();
    }
}
