package com.fruit.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fruit.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("outbound_order")
public class OutboundOrder extends BaseEntity {

    private String orderCode;
    private Long warehouseId;
    private Integer orderStatus;
    private Integer orderType;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expectedDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualDate;
    
    private String remark;
    private Long creatorId;
    private Long reviewerId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;
}
