package com.fruit.warehouse.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OutboundOrderDetailVO {

    private Long id;
    private Long outboundOrderId;
    private Long productId;
    private String productName;
    private String productCode;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal actualQuantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String remark;
}
