package com.fruit.warehouse.module.purchase.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PurchaseOrderItemVO {
    private Long id;
    private Long purchaseOrderId;
    private Long fruitId;
    private String fruitName;
    private String batchNo;
    private LocalDate productionDate;
    private LocalDate expirationDate;
    private BigDecimal quantity;
    private BigDecimal receivedQty;
    private BigDecimal pendingQty;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
