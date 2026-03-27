package com.fruit.warehouse.module.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WarningItemPoint {
    private Long alertId;
    private String alertType;
    private String alertLevel;
    private String alertMsg;
    private BigDecimal thresholdValue;
    private BigDecimal currentValue;
    private LocalDateTime createdTime;

    private Long fruitId;
    private String fruitName;

    private Long warehouseId;
    private String warehouseName;

    private Long batchId;
    private String batchNo;
}
