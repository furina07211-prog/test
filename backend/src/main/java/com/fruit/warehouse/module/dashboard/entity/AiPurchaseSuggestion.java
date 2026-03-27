package com.fruit.warehouse.module.dashboard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ai_purchase_suggestion")
public class AiPurchaseSuggestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fruitId;
    private Long warehouseId;
    private LocalDate suggestionDate;
    private BigDecimal predictedDailyQty;
    private Integer leadTimeDays;
    private BigDecimal safetyStockQty;
    private BigDecimal currentStockQty;
    private BigDecimal inTransitQty;
    private BigDecimal recommendedPurchaseQty;
    private String status;
    private String reason;
    private LocalDateTime createdTime;
}