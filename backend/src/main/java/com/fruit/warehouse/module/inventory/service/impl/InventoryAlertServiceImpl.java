package com.fruit.warehouse.module.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.warehouse.module.inventory.entity.InventoryAlert;
import com.fruit.warehouse.module.inventory.mapper.InventoryAlertMapper;
import com.fruit.warehouse.module.inventory.service.InventoryAlertService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 库存管理 模块服务实现。
 */
@Service
@RequiredArgsConstructor
public class InventoryAlertServiceImpl extends ServiceImpl<InventoryAlertMapper, InventoryAlert> implements InventoryAlertService {

    @Override
    public List<InventoryAlert> listActive() {
        LambdaQueryWrapper<InventoryAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryAlert::getAlertStatus, "UNHANDLED");
        wrapper.orderByDesc(InventoryAlert::getCreatedTime);
        return this.list(wrapper);
    }

    @Override
    public void handleAlert(Long alertId, Long handlerId) {
        InventoryAlert alert = this.getById(alertId);
        if (alert != null) {
            alert.setAlertStatus("HANDLED");
            alert.setHandledBy(handlerId);
            alert.setHandledTime(LocalDateTime.now());
            this.updateById(alert);
        }
    }
}
