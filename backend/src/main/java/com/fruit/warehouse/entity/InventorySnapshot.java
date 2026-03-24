package com.fruit.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("inventory_snapshot")
public class InventorySnapshot {

    @TableId(type = IdType.AUTO)
    private Long id;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date snapshotDate;
    
    private Long productId;
    private Long warehouseId;
    private BigDecimal totalQuantity;
    
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
