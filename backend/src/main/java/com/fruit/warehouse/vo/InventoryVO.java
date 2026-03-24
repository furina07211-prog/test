package com.fruit.warehouse.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class InventoryVO {

    private Long id;
    private Long productId;
    private String productCode;
    private String productName;
    private String categoryName;
    private String unit;
    private Long warehouseId;
    private String warehouseName;
    private BigDecimal totalQuantity;
    private BigDecimal lockedQuantity;
    private BigDecimal availableQuantity;
    private Integer lowStockThreshold;
    private String stockStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    
    private List<BatchVO> batches;
}
