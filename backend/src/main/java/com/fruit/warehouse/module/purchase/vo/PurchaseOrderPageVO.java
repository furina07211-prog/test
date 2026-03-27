package com.fruit.warehouse.module.purchase.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PurchaseOrderPageVO {
    private Long id;
    private String purchaseNo;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseName;
    private String orderStatus;
    private LocalDate orderDate;
    private LocalDate expectedArrivalDate;
    private BigDecimal totalAmount;
    private Long createdBy;
    private Long receivedBy;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private BigDecimal totalQty;
    private BigDecimal receivedQty;
    private BigDecimal pendingQty;
}
