package com.fruit.warehouse.module.sales.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SalesOrderItemVO {
    private Long id;
    private Long salesOrderId;
    private Long fruitId;
    private String fruitName;
    private Long batchId;
    private BigDecimal quantity;
    private BigDecimal shippedQty;
    private BigDecimal pendingQty;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
