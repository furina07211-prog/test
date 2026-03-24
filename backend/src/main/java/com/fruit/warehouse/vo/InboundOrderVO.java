package com.fruit.warehouse.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class InboundOrderVO {

    private Long id;
    private String orderCode;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseName;
    private Integer orderStatus;
    private String orderStatusDesc;
    private BigDecimal totalAmount;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expectedDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date actualDate;
    
    private String remark;
    private Long creatorId;
    private String creatorName;
    private Long reviewerId;
    private String reviewerName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    private List<InboundOrderDetailVO> details;
}
