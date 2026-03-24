package com.fruit.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("inbound_order_detail")
public class InboundOrderDetail {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long inboundOrderId;
    private Long productId;
    private BigDecimal expectedQuantity;
    private BigDecimal actualQuantity;
    private BigDecimal unitCost;
    private BigDecimal amount;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date productionDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expiryDate;
    
    private Long batchId;
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
