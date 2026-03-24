package com.fruit.warehouse.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("outbound_order_detail")
public class OutboundOrderDetail {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long outboundOrderId;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal actualQuantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
