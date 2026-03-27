package com.fruit.warehouse.module.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.warehouse.module.inventory.dto.InventoryQueryRequest;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import java.math.BigDecimal;
import java.util.List;

public interface InventoryService extends IService<InventoryBatch> {
    IPage<InventoryBatch> pageQuery(InventoryQueryRequest request);

    List<InventoryBatch> listLowStock();

    List<InventoryBatch> listNearExpire(int warningDays);

    void adjustStock(Long batchId, BigDecimal deltaQty, String bizType, Long bizId, Long operatorId);

    InventoryBatch createOrIncreaseBatch(InventoryBatch incoming, BigDecimal inQty, String bizType, Long bizId, Long operatorId);
}
