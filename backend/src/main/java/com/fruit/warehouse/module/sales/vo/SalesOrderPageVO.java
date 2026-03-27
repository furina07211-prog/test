package com.fruit.warehouse.module.sales.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SalesOrderPageVO {
    private Long id;
    private String salesNo;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private String orderStatus;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    private Long createdBy;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private BigDecimal totalQty;
    private BigDecimal shippedQty;
    private BigDecimal pendingQty;
}
