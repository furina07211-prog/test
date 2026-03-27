package com.fruit.warehouse.module.dashboard.dto;

import com.fruit.warehouse.module.dashboard.entity.AiForecastResult;
import lombok.Data;

import java.util.List;

@Data
public class ForecastTrendResponse {
    private Long fruitId;
    private String fruitName;
    private List<DailyPoint> history;
    private List<AiForecastResult> forecast;
}