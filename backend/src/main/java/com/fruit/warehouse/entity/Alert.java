package com.fruit.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fruit.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("alert")
public class Alert extends BaseEntity {

    private Integer alertType;
    private Long productId;
    private Long warehouseId;
    private Long batchId;
    private String alertContent;
    private BigDecimal thresholdValue;
    private BigDecimal currentValue;
    private Integer status;
    private Long handlerId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;
    
    private String handleRemark;
}
