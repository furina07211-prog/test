package com.fruit.warehouse.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.entity.Alert;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.AlertService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "预警管理")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @ApiOperation("分页查询预警")
    @GetMapping
    @RequirePermission({"alert:list"})
    public Result<Page<Alert>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer alertType,
            @RequestParam(required = false) Integer status) {
        return Result.success(alertService.page(pageNum, pageSize, alertType, status));
    }

    @ApiOperation("获取未读预警数量")
    @GetMapping("/count")
    public Result<Integer> countUnread() {
        return Result.success(alertService.countUnread());
    }

    @ApiOperation("标记已读")
    @PutMapping("/{id}/read")
    @RequirePermission({"alert:handle"})
    public Result<Void> markRead(@PathVariable Long id) {
        alertService.markRead(id);
        return Result.success();
    }

    @ApiOperation("处理预警")
    @PutMapping("/{id}/handle")
    @RequirePermission({"alert:handle"})
    public Result<Void> handle(@PathVariable Long id, @RequestBody Map<String, String> params) {
        alertService.handle(id, params.get("remark"));
        return Result.success();
    }
}
