package com.fruit.warehouse.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardVO {

    private Integer totalProducts;
    private Integer totalSuppliers;
    private Integer totalWarehouses;
    private BigDecimal totalInventory;
    private Integer todayInboundCount;
    private Integer todayOutboundCount;
    private Integer activeAlerts;
    private Integer pendingInboundOrders;
    private Integer pendingOutboundOrders;
}
