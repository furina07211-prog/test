package com.fruit.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fruit.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inventory_batch")
public class InventoryBatch extends BaseEntity {

    private String batchCode;
    private Long productId;
    private Long warehouseId;
    private Long supplierId;
    private Long inboundOrderId;
    private BigDecimal quantity;
    private BigDecimal remainingQuantity;
    private BigDecimal unitCost;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date productionDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expiryDate;
    
    private Integer batchStatus;
    
    @Version
    private Integer version;
}
