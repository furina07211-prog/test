package com.fruit.warehouse.task;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.warehouse.entity.Alert;
import com.fruit.warehouse.entity.InventoryBatch;
import com.fruit.warehouse.entity.Product;
import com.fruit.warehouse.enums.AlertType;
import com.fruit.warehouse.enums.BatchStatus;
import com.fruit.warehouse.mapper.AlertMapper;
import com.fruit.warehouse.mapper.InventoryBatchMapper;
import com.fruit.warehouse.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiryAlertTask {

    private final InventoryBatchMapper batchMapper;
    private final ProductMapper productMapper;
    private final AlertMapper alertMapper;

    @Value("${alert.expiry-warning-days:3}")
    private int expiryWarningDays;

    @Scheduled(cron = "${alert.expiry-check-cron:0 0 7 * * ?}")
    public void checkExpiry() {
        log.info("Starting expiry check task...");
        
        Date today = new Date();
        Date warningDate = DateUtil.offsetDay(today, expiryWarningDays);
        
        // Find batches that are normal or expiring and have remaining quantity
        LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(InventoryBatch::getBatchStatus, BatchStatus.NORMAL.getCode(), BatchStatus.EXPIRING.getCode())
               .gt(InventoryBatch::getRemainingQuantity, 0);
        
        List<InventoryBatch> batches = batchMapper.selectList(wrapper);
        
        int expiredCount = 0;
        int expiringCount = 0;
        
        for (InventoryBatch batch : batches) {
            if (batch.getExpiryDate().before(today)) {
                // Already expired
                batch.setBatchStatus(BatchStatus.EXPIRED.getCode());
                batchMapper.updateById(batch);
                createAlert(batch, AlertType.EXPIRED);
                expiredCount++;
            } else if (batch.getExpiryDate().before(warningDate)) {
                // Expiring soon
                if (batch.getBatchStatus() != BatchStatus.EXPIRING.getCode()) {
                    batch.setBatchStatus(BatchStatus.EXPIRING.getCode());
                    batchMapper.updateById(batch);
                    createAlert(batch, AlertType.EXPIRING_SOON);
                    expiringCount++;
                }
            }
        }
        
        log.info("Expiry check completed: {} expired, {} expiring soon", expiredCount, expiringCount);
    }
    
    private void createAlert(InventoryBatch batch, AlertType type) {
        // Check if alert already exists for this batch
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Alert::getBatchId, batch.getId())
               .eq(Alert::getAlertType, type.getCode())
               .eq(Alert::getStatus, 0);
        if (alertMapper.selectCount(wrapper) > 0) {
            return; // Alert already exists
        }
        
        Product product = productMapper.selectById(batch.getProductId());
        
        Alert alert = new Alert();
        alert.setAlertType(type.getCode());
        alert.setProductId(batch.getProductId());
        alert.setWarehouseId(batch.getWarehouseId());
        alert.setBatchId(batch.getId());
        
        long days = DateUtil.betweenDay(new Date(), batch.getExpiryDate(), false);
        String content = String.format("商品【%s】批次【%s】%s，剩余%d天，库存%s",
                product != null ? product.getProductName() : "Unknown",
                batch.getBatchCode(),
                type == AlertType.EXPIRED ? "已过期" : "即将过期",
                days,
                batch.getRemainingQuantity());
        
        alert.setAlertContent(content);
        alert.setStatus(0);
        alertMapper.insert(alert);
    }
}
