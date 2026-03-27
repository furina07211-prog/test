package com.fruit.warehouse.module.dashboard.controller;

import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.common.result.Results;
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

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<DashboardOverviewResponse> overview() {
        return Results.ok(dashboardService.getOverview());
    }

    @GetMapping("/amount-trend")
    public Result<List<TrendAmountPoint>> amountTrend(@RequestParam(required = false, defaultValue = "7") Integer days) {
        return Results.ok(dashboardService.getAmountTrend(days));
    }

    @GetMapping("/inventory/category-ratio")
    public Result<List<InventoryCategoryRatioPoint>> categoryRatio() {
        return Results.ok(dashboardService.getInventoryCategoryRatio());
    }

    @GetMapping("/sales-top")
    public Result<List<SalesTopPoint>> salesTop(@RequestParam(required = false, defaultValue = "30") Integer days,
                                                @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return Results.ok(dashboardService.getSalesTop(days, limit));
    }

    @GetMapping("/warnings")
    public Result<List<WarningItemPoint>> warnings(@RequestParam(required = false, defaultValue = "20") Integer limit) {
        return Results.ok(dashboardService.getWarnings(limit));
    }

    @PostMapping("/forecast/run")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Map<String, Object>> runForecast(@RequestBody ForecastRunRequest request) {
        return Results.ok(dashboardService.runForecast(request));
    }

    @GetMapping("/forecast/trend")
    public Result<ForecastTrendResponse> forecastTrend(@RequestParam Long fruitId,
                                                       @RequestParam(required = false, defaultValue = "30") Integer historyDays) {
        return Results.ok(dashboardService.getForecastTrend(fruitId, historyDays));
    }

    @PostMapping("/inventory/optimize/run")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<List<AiPurchaseSuggestion>> runOptimize(@RequestBody OptimizeRunRequest request) {
        return Results.ok(dashboardService.runOptimize(request));
    }

    @GetMapping("/inventory/optimize/list")
    public Result<List<AiPurchaseSuggestion>> listOptimize(@RequestParam(required = false) Long fruitId,
                                                           @RequestParam(required = false) Long warehouseId,
                                                           @RequestParam(required = false, defaultValue = "20") Integer limit) {
        return Results.ok(dashboardService.listOptimize(fruitId, warehouseId, limit));
    }

    @GetMapping("/alert-heatmap")
    public Result<List<HeatmapPoint>> alertHeatmap() {
        return Results.ok(dashboardService.getAlertHeatmap());
    }
}
