package com.fruit.warehouse.module.purchase.dto;

import lombok.Data;

@Data
public class PurchaseOrderPageQuery {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String purchaseNo;
    private String status;
    private Long supplierId;
    private Long warehouseId;
}
