package com.fruit.warehouse.controller;

import com.fruit.warehouse.common.Result;
import com.fruit.warehouse.security.RequirePermission;
import com.fruit.warehouse.service.StatisticsService;
import com.fruit.warehouse.vo.DashboardVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "统计分析")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @ApiOperation("获取仪表盘数据")
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        return Result.success(statisticsService.getDashboard());
    }

    @ApiOperation("获取库存趋势")
    @GetMapping("/inventory-trend")
    @RequirePermission({"stats:view"})
    public Result<List<Map<String, Object>>> getInventoryTrend(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(statisticsService.getInventoryTrend(productId, warehouseId, startDate, endDate));
    }

    @ApiOperation("获取出入库统计")
    @GetMapping("/inout-stats")
    @RequirePermission({"stats:view"})
    public Result<List<Map<String, Object>>> getInOutStats(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return Result.success(statisticsService.getInOutStats(warehouseId, startDate, endDate));
    }

    @ApiOperation("获取商品出库排行")
    @GetMapping("/product-ranking")
    @RequirePermission({"stats:view"})
    public Result<List<Map<String, Object>>> getProductRanking(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statisticsService.getProductRanking(startDate, endDate, limit));
    }
}
