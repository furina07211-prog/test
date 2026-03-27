package com.fruit.warehouse.module.sales.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class SalesShipRequest {
    private Long operatorId;
    private List<ShipItem> items;

    @Data
    public static class ShipItem {
        private Long itemId;
        private BigDecimal shipQty;
    }
}
