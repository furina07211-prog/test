package com.fruit.warehouse.module.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.warehouse.module.inventory.dto.InventoryQueryRequest;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import java.math.BigDecimal;
import java.util.List;

/**
 * 库存管理 模块服务接口。
 */
public interface InventoryService extends IService<InventoryBatch> {
    /**
     * 条件分页查询库存批次。
     */
    IPage<InventoryBatch> pageQuery(InventoryQueryRequest request);

    /**
     * 查询低于安全库存的批次。
     */
    List<InventoryBatch> listLowStock();

    /**
     * 查询临期批次。
     */
    List<InventoryBatch> listNearExpire(int warningDays);

    /**
     * 调整批次库存并记录库存流水。
     */
    void adjustStock(Long batchId, BigDecimal deltaQty, String bizType, Long bizId, Long operatorId);

    /**
     * 创建新批次或累加既有批次库存。
     */
    InventoryBatch createOrIncreaseBatch(InventoryBatch incoming, BigDecimal inQty, String bizType, Long bizId, Long operatorId);
}
