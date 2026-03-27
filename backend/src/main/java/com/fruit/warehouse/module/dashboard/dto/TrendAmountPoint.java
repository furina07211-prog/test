package com.fruit.warehouse.module.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TrendAmountPoint {
    private LocalDate date;
    private BigDecimal purchaseAmount;
    private BigDecimal salesAmount;
}
