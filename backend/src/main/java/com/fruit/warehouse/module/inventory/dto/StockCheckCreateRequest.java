package com.fruit.warehouse.module.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class StockCheckCreateRequest {
    private Long warehouseId;
    private LocalDate checkDate;
    private String remark;
    private List<StockCheckItemRequest> items;

    @Data
    public static class StockCheckItemRequest {
        private Long batchId;
        private BigDecimal actualQty;
        private String reason;
    }
}
