package com.fruit.warehouse.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("inventory_txn")
public class InventoryTxn {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String bizType;
    private Long bizId;
    private Long batchId;
    private Long fruitId;
    private Long warehouseId;
    private BigDecimal changeQty;
    private BigDecimal balanceQty;
    private Long operatorId;
    private LocalDateTime txnTime;
    private String remark;
}
