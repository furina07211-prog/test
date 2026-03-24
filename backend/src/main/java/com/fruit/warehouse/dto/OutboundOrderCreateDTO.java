package com.fruit.warehouse.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OutboundOrderCreateDTO {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private Integer orderType;
    private String customerName;
    private String customerPhone;
    private Date expectedDate;
    private String remark;

    private List<DetailItem> details;

    @Data
    public static class DetailItem {
        @NotNull(message = "商品不能为空")
        private Long productId;
        
        @NotNull(message = "数量不能为空")
        private BigDecimal quantity;
        
        @NotNull(message = "单价不能为空")
        private BigDecimal unitPrice;
        
        private String remark;
    }
}
