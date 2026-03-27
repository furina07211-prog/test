package com.fruit.warehouse.module.inventory.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class InventoryQueryRequest {
    private int pageNo = 1;
    private int pageSize = 10;
    private Long fruitId;
    private Long warehouseId;
    private String status;
    private LocalDate expirationFrom;
    private LocalDate expirationTo;
}
