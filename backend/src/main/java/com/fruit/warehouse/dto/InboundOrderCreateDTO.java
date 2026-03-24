package com.fruit.warehouse.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class InboundOrderCreateDTO {

    @NotNull(message = "供应商不能为空")
    private Long supplierId;

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private Date expectedDate;
    private String remark;

    private List<DetailItem> details;

    @Data
    public static class DetailItem {
        @NotNull(message = "商品不能为空")
        private Long productId;
        
        @NotNull(message = "数量不能为空")
        private BigDecimal expectedQuantity;
        
        @NotNull(message = "单价不能为空")
        private BigDecimal unitCost;
        
        private Date productionDate;
        private String remark;
    }
}
