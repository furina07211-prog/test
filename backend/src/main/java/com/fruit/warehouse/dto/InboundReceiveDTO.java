package com.fruit.warehouse.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class InboundReceiveDTO {

    @NotNull(message = "入库单ID不能为空")
    private Long orderId;

    private List<ReceiveItem> items;

    @Data
    public static class ReceiveItem {
        @NotNull(message = "明细ID不能为空")
        private Long detailId;
        
        @NotNull(message = "实收数量不能为空")
        private BigDecimal actualQuantity;
        
        private Date productionDate;
    }
}
