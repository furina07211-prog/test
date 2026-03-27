package com.fruit.warehouse.module.dashboard.service.impl;

import com.fruit.warehouse.module.dashboard.config.DashboardProperties;
import com.fruit.warehouse.module.dashboard.dto.ForecastRunRequest;
import com.fruit.warehouse.module.dashboard.dto.OptimizeRunRequest;
import com.fruit.warehouse.module.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardSchedule {

    private final DashboardService dashboardService;
    private final DashboardProperties dashboardProperties;

    @Scheduled(cron = "${fruit.algorithm.auto-cron:0 30 2 * * ?}")
    public void autoRefresh() {
        if (!dashboardProperties.isAutoEnabled()) {
            return;
        }
        try {
            ForecastRunRequest forecastRequest = new ForecastRunRequest();
            forecastRequest.setDays(7);
            forecastRequest.setModel("prophet");
            dashboardService.runForecast(forecastRequest);

            OptimizeRunRequest optimizeRunRequest = new OptimizeRunRequest();
            optimizeRunRequest.setSafetyDays(dashboardProperties.getDefaultSafetyDays());
            dashboardService.runOptimize(optimizeRunRequest);
        } catch (Exception ex) {
            log.error("auto forecast/optimize failed", ex);
        }
    }
}