package com.fruit.warehouse.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("inventory_alert")
public class InventoryAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String alertType;
    private Long fruitId;
    private Long batchId;
    private Long warehouseId;
    private String alertLevel;
    private String alertMsg;
    private BigDecimal thresholdValue;
    private BigDecimal currentValue;
    private String alertStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
    private LocalDateTime handledTime;
    private Long handledBy;
}
