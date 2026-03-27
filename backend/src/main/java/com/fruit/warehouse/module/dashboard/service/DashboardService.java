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

/**
 * 数据看板 模块服务接口。
 */
public interface DashboardService {
    /**
     * 获取看板核心概览指标。
     */
    DashboardOverviewResponse getOverview();

    /**
     * 获取金额趋势（采购/销售）。
     */
    List<TrendAmountPoint> getAmountTrend(Integer days);

    /**
     * 获取库存分类占比。
     */
    List<InventoryCategoryRatioPoint> getInventoryCategoryRatio();

    /**
     * 获取销量排行。
     */
    List<SalesTopPoint> getSalesTop(Integer days, Integer limit);

    /**
     * 获取预警列表。
     */
    List<WarningItemPoint> getWarnings(Integer limit);

    /**
     * 执行销量预测。
     */
    Map<String, Object> runForecast(ForecastRunRequest request);

    /**
     * 获取历史+预测趋势。
     */
    ForecastTrendResponse getForecastTrend(Long fruitId, Integer historyDays);

    /**
     * 执行库存优化建议计算。
     */
    List<AiPurchaseSuggestion> runOptimize(OptimizeRunRequest request);

    /**
     * 查询库存优化建议列表。
     */
    List<AiPurchaseSuggestion> listOptimize(Long fruitId, Long warehouseId, Integer limit);

    /**
     * 获取预警热力图数据。
     */
    List<HeatmapPoint> getAlertHeatmap();
}
