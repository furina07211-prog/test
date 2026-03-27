package com.fruit.warehouse.module.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class PurchaseOrderCreateRequest {
    private Long supplierId;
    private Long warehouseId;
    private LocalDate orderDate;
    private LocalDate expectedArrivalDate;
    private String remark;
    private List<PurchaseItemRequest> items;

    @Data
    public static class PurchaseItemRequest {
        private Long fruitId;
        private String batchNo;
        private LocalDate productionDate;
        private LocalDate expirationDate;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private String remark;
    }
}
