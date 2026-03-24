package com.fruit.warehouse.service;

import com.fruit.warehouse.vo.DashboardVO;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    DashboardVO getDashboard();

    List<Map<String, Object>> getInventoryTrend(Long productId, Long warehouseId, String startDate, String endDate);

    List<Map<String, Object>> getInOutStats(Long warehouseId, String startDate, String endDate);

    List<Map<String, Object>> getProductRanking(String startDate, String endDate, Integer limit);
}
