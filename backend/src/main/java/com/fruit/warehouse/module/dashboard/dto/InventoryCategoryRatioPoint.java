package com.fruit.warehouse.module.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryCategoryRatioPoint {
    private Long categoryId;
    private String categoryName;
    private BigDecimal stockQty;
}
