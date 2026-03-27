package com.fruit.warehouse.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Inventory batch entity mapping inventory_batch table.
 */
@Data
@TableName("inventory_batch")
public class InventoryBatch {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fruitId;
    private Long warehouseId;
    private String batchNo;
    private String sourceType;
    private Long sourceId;
    private LocalDate productionDate;
    private LocalDate expirationDate;
    private BigDecimal totalQty;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal unitCost;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    /** derived safe stock from fruit_info.safe_stock_qty */
    @TableField(exist = false)
    private BigDecimal safeStockQty;
    /** derived warning days from fruit_info.warning_days */
    @TableField(exist = false)
    private Integer warningDays;
}
