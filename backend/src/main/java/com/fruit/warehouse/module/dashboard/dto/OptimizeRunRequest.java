package com.fruit.warehouse.module.dashboard.dto;

import lombok.Data;

@Data
public class OptimizeRunRequest {
    private Long fruitId;
    private Long warehouseId;
    private Integer leadTimeDays = 1;
    private Integer safetyDays = 3;
}