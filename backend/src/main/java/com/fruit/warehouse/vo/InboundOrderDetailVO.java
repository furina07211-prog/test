package com.fruit.warehouse.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class InboundOrderDetailVO {

    private Long id;
    private Long inboundOrderId;
    private Long productId;
    private String productName;
    private String productCode;
    private String unit;
    private BigDecimal expectedQuantity;
    private BigDecimal actualQuantity;
    private BigDecimal unitCost;
    private BigDecimal amount;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date productionDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expiryDate;
    
    private Long batchId;
    private String batchCode;
    private String remark;
}
