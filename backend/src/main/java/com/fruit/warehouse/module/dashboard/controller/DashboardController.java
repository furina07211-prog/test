package com.fruit.warehouse.module.dashboard.controller;

import com.fruit.warehouse.common.result.Results;
import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.module.dashboard.dto.DashboardOverviewResponse;
import com.fruit.warehouse.module.dashboard.dto.ForecastRunRequest;
import com.fruit.warehouse.module.dashboard.dto.ForecastTrendResponse;
import com.fruit.warehouse.module.dashboard.dto.HeatmapPoint;
import com.fruit.warehouse.module.dashboard.dto.InventoryCategoryRatioPoint;
import com.fruit.warehouse.module.dashboard.dto.OptimizeRunRequest;
import com.fruit.warehouse.module.dashboard.dto.SalesTopPoint;
import com.fruit.warehouse.module.dashboard.dto.TrendAmountPoint;
import com.fruit.warehouse.module.dashboard.dto.WarningItemPoint;
import com.fruit.warehouse.module.dashboard.entity.AiPurchaseSuggestion;
import com.fruit.warehouse.module.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据看板 模块控制器。
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 看板核心概览指标。
     */
    @GetMapping("/overview")
    public Result<DashboardOverviewResponse> overview() {
        return Results.ok(dashboardService.getOverview());
    }

    /**
     * 采购/销售金额趋势（按天）。
     */
    @GetMapping("/amount-trend")
    public Result<List<TrendAmountPoint>> amountTrend(@RequestParam(required = false, defaultValue = "7") Integer days) {
        return Results.ok(dashboardService.getAmountTrend(days));
    }

    /**
     * 按水果分类统计库存占比。
     */
    @GetMapping("/inventory/category-ratio")
    public Result<List<InventoryCategoryRatioPoint>> categoryRatio() {
        return Results.ok(dashboardService.getInventoryCategoryRatio());
    }

    /**
     * 销量 Top 榜单。
     */
    @GetMapping("/sales-top")
    public Result<List<SalesTopPoint>> salesTop(@RequestParam(required = false, defaultValue = "30") Integer days,
                                                @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return Results.ok(dashboardService.getSalesTop(days, limit));
    }

    /**
     * 查询未处理库存预警列表。
     */
    @GetMapping("/warnings")
    public Result<List<WarningItemPoint>> warnings(@RequestParam(required = false, defaultValue = "20") Integer limit) {
        return Results.ok(dashboardService.getWarnings(limit));
    }

    /**
     * 触发销量预测任务。
     */
    @PostMapping("/forecast/run")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Map<String, Object>> runForecast(@RequestBody ForecastRunRequest request) {
        return Results.ok(dashboardService.runForecast(request));
    }

    /**
     * 查询历史+预测趋势曲线。
     */
    @GetMapping("/forecast/trend")
    public Result<ForecastTrendResponse> forecastTrend(@RequestParam Long fruitId,
                                                       @RequestParam(required = false, defaultValue = "30") Integer historyDays) {
        return Results.ok(dashboardService.getForecastTrend(fruitId, historyDays));
    }

    /**
     * 触发库存优化建议生成。
     */
    @PostMapping("/inventory/optimize/run")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<List<AiPurchaseSuggestion>> runOptimize(@RequestBody OptimizeRunRequest request) {
        return Results.ok(dashboardService.runOptimize(request));
    }

    /**
     * 查询库存优化建议列表。
     */
    @GetMapping("/inventory/optimize/list")
    public Result<List<AiPurchaseSuggestion>> listOptimize(@RequestParam(required = false) Long fruitId,
                                                           @RequestParam(required = false) Long warehouseId,
                                                           @RequestParam(required = false, defaultValue = "20") Integer limit) {
        return Results.ok(dashboardService.listOptimize(fruitId, warehouseId, limit));
    }

    /**
     * 查询预警热力图数据。
     */
    @GetMapping("/alert-heatmap")
    public Result<List<HeatmapPoint>> alertHeatmap() {
        return Results.ok(dashboardService.getAlertHeatmap());
    }
}
