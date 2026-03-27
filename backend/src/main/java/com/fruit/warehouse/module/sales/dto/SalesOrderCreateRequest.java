package com.fruit.warehouse.module.sales.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SalesOrderCreateRequest {
    private Long customerId;
    private Long warehouseId;
    private LocalDateTime orderTime;
    private String remark;
    private List<SalesItemRequest> items;

    @Data
    public static class SalesItemRequest {
        private Long fruitId;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private String remark;
    }
}
