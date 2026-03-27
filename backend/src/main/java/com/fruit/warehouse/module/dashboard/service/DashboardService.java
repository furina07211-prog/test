package com.fruit.warehouse.module.dashboard.service;

import com.fruit.warehouse.module.dashboard.dto.ForecastRunRequest;
import com.fruit.warehouse.module.dashboard.dto.ForecastTrendResponse;
import com.fruit.warehouse.module.dashboard.dto.HeatmapPoint;
import com.fruit.warehouse.module.dashboard.dto.OptimizeRunRequest;
import com.fruit.warehouse.module.dashboard.entity.AiPurchaseSuggestion;

import java.util.List;
import java.util.Map;

public interface DashboardService {
    Map<String, Object> runForecast(ForecastRunRequest request);

    ForecastTrendResponse getForecastTrend(Long fruitId, Integer historyDays);

    List<AiPurchaseSuggestion> runOptimize(OptimizeRunRequest request);

    List<AiPurchaseSuggestion> listOptimize(Long fruitId, Long warehouseId, Integer limit);

    List<HeatmapPoint> getAlertHeatmap();
}