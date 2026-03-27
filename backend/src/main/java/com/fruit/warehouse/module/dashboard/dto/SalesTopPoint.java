package com.fruit.warehouse.module.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesTopPoint {
    private Long fruitId;
    private String fruitName;
    private BigDecimal salesQty;
    private BigDecimal salesAmount;
}
