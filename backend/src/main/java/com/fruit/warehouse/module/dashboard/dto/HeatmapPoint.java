package com.fruit.warehouse.module.dashboard.dto;

import lombok.Data;

@Data
public class HeatmapPoint {
    private String fruitName;
    private String alertType;
    private Integer alertCount;
}