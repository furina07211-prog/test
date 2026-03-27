package com.fruit.warehouse.module.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.warehouse.module.inventory.entity.InventoryAlert;
import java.util.List;

/**
 * 库存管理 模块服务接口。
 */
public interface InventoryAlertService extends IService<InventoryAlert> {
    List<InventoryAlert> listActive();

    void handleAlert(Long alertId, Long handlerId);
}
