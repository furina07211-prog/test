package com.fruit.warehouse.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BatchVO {

    private Long id;
    private String batchCode;
    private Long productId;
    private String productName;
    private Long warehouseId;
    private String warehouseName;
    private Long supplierId;
    private String supplierName;
    private BigDecimal quantity;
    private BigDecimal remainingQuantity;
    private BigDecimal unitCost;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date productionDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expiryDate;
    
    private Integer remainingDays;
    private Integer batchStatus;
    private String batchStatusDesc;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
