package com.fruit.warehouse.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OutboundOrderVO {

    private Long id;
    private String orderCode;
    private Long warehouseId;
    private String warehouseName;
    private Integer orderStatus;
    private String orderStatusDesc;
    private Integer orderType;
    private String orderTypeDesc;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expectedDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualDate;
    
    private String remark;
    private Long creatorId;
    private String creatorName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    private List<OutboundOrderDetailVO> details;
}
