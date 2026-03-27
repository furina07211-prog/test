package com.fruit.warehouse.module.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockSummaryPoint {
    private Long fruitId;
    private String fruitName;
    private Long warehouseId;
    private BigDecimal currentStockQty;
    private BigDecimal safeStockQty;
    private BigDecimal inTransitQty;
}