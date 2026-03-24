package com.fruit.warehouse.task;

import cn.hutool.core.date.DateUtil;
import com.fruit.warehouse.entity.Inventory;
import com.fruit.warehouse.entity.InventorySnapshot;
import com.fruit.warehouse.mapper.InventoryMapper;
import com.fruit.warehouse.mapper.InventorySnapshotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventorySnapshotTask {

    private final InventoryMapper inventoryMapper;
    private final InventorySnapshotMapper snapshotMapper;

    @Scheduled(cron = "${alert.snapshot-cron:0 0 1 * * ?}")
    public void createSnapshot() {
        log.info("Starting inventory snapshot task...");
        
        Date yesterday = DateUtil.offsetDay(new Date(), -1);
        Date snapshotDate = DateUtil.beginOfDay(yesterday);
        
        List<Inventory> inventories = inventoryMapper.selectList(null);
        int count = 0;
        
        for (Inventory inventory : inventories) {
            InventorySnapshot snapshot = new InventorySnapshot();
            snapshot.setSnapshotDate(snapshotDate);
            snapshot.setProductId(inventory.getProductId());
            snapshot.setWarehouseId(inventory.getWarehouseId());
            snapshot.setTotalQuantity(inventory.getTotalQuantity());
            
            try {
                snapshotMapper.insert(snapshot);
                count++;
            } catch (Exception e) {
                // Ignore duplicate key errors
                log.debug("Snapshot already exists for product {} warehouse {} on {}", 
                        inventory.getProductId(), inventory.getWarehouseId(), snapshotDate);
            }
        }
        
        log.info("Inventory snapshot completed: {} snapshots created", count);
    }
}
