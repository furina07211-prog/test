package com.fruit.warehouse.module.dashboard.service;

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

import java.util.List;
import java.util.Map;

public interface DashboardService {
    DashboardOverviewResponse getOverview();

    List<TrendAmountPoint> getAmountTrend(Integer days);

    List<InventoryCategoryRatioPoint> getInventoryCategoryRatio();

    List<SalesTopPoint> getSalesTop(Integer days, Integer limit);

    List<WarningItemPoint> getWarnings(Integer limit);

    Map<String, Object> runForecast(ForecastRunRequest request);

    ForecastTrendResponse getForecastTrend(Long fruitId, Integer historyDays);

    List<AiPurchaseSuggestion> runOptimize(OptimizeRunRequest request);

    List<AiPurchaseSuggestion> listOptimize(Long fruitId, Long warehouseId, Integer limit);

    List<HeatmapPoint> getAlertHeatmap();
}
