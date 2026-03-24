package com.fruit.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("inbound_record")
public class InboundRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long inboundOrderId;
    private Long inboundDetailId;
    private Long productId;
    private Long warehouseId;
    private Long batchId;
    private BigDecimal quantity;
    private Long operatorId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date operateTime;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
