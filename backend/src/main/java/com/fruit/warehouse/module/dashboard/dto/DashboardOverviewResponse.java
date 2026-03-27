package com.fruit.warehouse.module.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardOverviewResponse {
    private Long totalStockSku;
    private BigDecimal todayPurchaseAmount;
    private BigDecimal todaySalesAmount;
    private Long warningFruitCount;
}
