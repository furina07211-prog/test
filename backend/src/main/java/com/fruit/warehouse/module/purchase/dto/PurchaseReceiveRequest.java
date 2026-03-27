package com.fruit.warehouse.module.purchase.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class PurchaseReceiveRequest {
    private Long operatorId;
    private List<ReceiveItem> items;

    @Data
    public static class ReceiveItem {
        private Long itemId;
        private BigDecimal receiveQty;
        private BigDecimal receivedQty;
        private BigDecimal unitCost;

        public BigDecimal resolveReceiveQty() {
            return receiveQty != null ? receiveQty : receivedQty;
        }
    }
}
