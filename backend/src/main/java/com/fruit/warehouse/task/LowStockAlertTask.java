package com.fruit.warehouse.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.warehouse.entity.Alert;
import com.fruit.warehouse.entity.Inventory;
import com.fruit.warehouse.entity.Product;
import com.fruit.warehouse.enums.AlertType;
import com.fruit.warehouse.mapper.AlertMapper;
import com.fruit.warehouse.mapper.InventoryMapper;
import com.fruit.warehouse.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockAlertTask {

    private final InventoryMapper inventoryMapper;
    private final ProductMapper productMapper;
    private final AlertMapper alertMapper;

    @Scheduled(cron = "${alert.low-stock-check-cron:0 0 8 * * ?}")
    public void checkLowStock() {
        log.info("Starting low stock check task...");
        
        List<Inventory> inventories = inventoryMapper.selectList(null);
        int alertCount = 0;
        
        for (Inventory inventory : inventories) {
            Product product = productMapper.selectById(inventory.getProductId());
            if (product == null) continue;
            
            if (inventory.getAvailableQuantity().intValue() < product.getLowStockThreshold()) {
                // Check if alert already exists
                LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Alert::getProductId, inventory.getProductId())
                       .eq(Alert::getWarehouseId, inventory.getWarehouseId())
                       .eq(Alert::getAlertType, AlertType.LOW_STOCK.getCode())
                       .eq(Alert::getStatus, 0);
                
                if (alertMapper.selectCount(wrapper) > 0) {
                    continue; // Alert already exists
                }
                
                Alert alert = new Alert();
                alert.setAlertType(AlertType.LOW_STOCK.getCode());
                alert.setProductId(inventory.getProductId());
                alert.setWarehouseId(inventory.getWarehouseId());
                alert.setThresholdValue(new java.math.BigDecimal(product.getLowStockThreshold()));
                alert.setCurrentValue(inventory.getAvailableQuantity());
                
                String content = String.format("商品【%s】库存不足，当前库存%s，低于阈值%d",
                        product.getProductName(),
                        inventory.getAvailableQuantity(),
                        product.getLowStockThreshold());
                
                alert.setAlertContent(content);
                alert.setStatus(0);
                alertMapper.insert(alert);
                alertCount++;
            }
        }
        
        log.info("Low stock check completed: {} alerts created", alertCount);
    }
}
