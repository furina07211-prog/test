package com.fruit.warehouse.module.sales.dto;

import lombok.Data;

@Data
public class SalesOrderPageQuery {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String salesNo;
    private String status;
    private Long customerId;
    private Long warehouseId;
}
