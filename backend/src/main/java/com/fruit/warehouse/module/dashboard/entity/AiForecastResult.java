package com.fruit.warehouse.module.dashboard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("ai_forecast_result")
public class AiForecastResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fruitId;
    private LocalDate forecastDate;
    private LocalDate targetDate;
    private String modelName;
    private String versionNo;
    private BigDecimal predictQty;
    private BigDecimal confidenceLower;
    private BigDecimal confidenceUpper;
    private Integer dataWindowDays;
    private LocalDateTime createdTime;
}