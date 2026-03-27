package com.fruit.warehouse.module.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("stock_check_item")
public class StockCheckItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long checkOrderId;
    private Long batchId;
    private Long fruitId;
    private BigDecimal systemQty;
    private BigDecimal actualQty;
    private BigDecimal diffQty;
    private String reason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
