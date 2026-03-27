package com.fruit.warehouse.module.dashboard.dto;

import lombok.Data;

@Data
public class ForecastRunRequest {
    private Long fruitId;
    private Integer days = 7;
    private String model = "prophet";
}