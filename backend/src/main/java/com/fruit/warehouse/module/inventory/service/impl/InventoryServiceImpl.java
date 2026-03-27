package com.fruit.warehouse.module.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.inventory.dto.InventoryQueryRequest;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import com.fruit.warehouse.module.inventory.entity.InventoryAlert;
import com.fruit.warehouse.module.inventory.entity.InventoryTxn;
import com.fruit.warehouse.module.inventory.mapper.InventoryAlertMapper;
import com.fruit.warehouse.module.inventory.mapper.InventoryBatchMapper;
import com.fruit.warehouse.module.inventory.mapper.InventoryTxnMapper;
import com.fruit.warehouse.module.inventory.service.InventoryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryBatchMapper, InventoryBatch> implements InventoryService {

    private final InventoryTxnMapper inventoryTxnMapper;
    private final InventoryAlertMapper inventoryAlertMapper;

    @Override
    public IPage<InventoryBatch> pageQuery(InventoryQueryRequest request) {
        LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getFruitId() != null, InventoryBatch::getFruitId, request.getFruitId());
        wrapper.eq(request.getWarehouseId() != null, InventoryBatch::getWarehouseId, request.getWarehouseId());
        wrapper.eq(request.getStatus() != null, InventoryBatch::getStatus, request.getStatus());
        if (request.getExpirationFrom() != null) {
            wrapper.ge(InventoryBatch::getExpirationDate, request.getExpirationFrom());
        }
        if (request.getExpirationTo() != null) {
            wrapper.le(InventoryBatch::getExpirationDate, request.getExpirationTo());
        }
        wrapper.orderByAsc(InventoryBatch::getExpirationDate);
        return this.page(new Page<>(request.getPageNo(), request.getPageSize()), wrapper);
    }

    @Override
    public List<InventoryBatch> listLowStock() {
        LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("available_qty <= (select safe_stock_qty from fruit_info f where f.id = inventory_batch.fruit_id)");
        wrapper.orderByAsc(InventoryBatch::getExpirationDate);
        return this.list(wrapper);
    }

    @Override
    public List<InventoryBatch> listNearExpire(int warningDays) {
        LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.apply("DATEDIFF(expiration_date, CURDATE()) <= {0}", warningDays);
        wrapper.orderByAsc(InventoryBatch::getExpirationDate);
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Long batchId, BigDecimal deltaQty, String bizType, Long bizId, Long operatorId) {
        InventoryBatch batch = this.getById(batchId);
        if (batch == null) {
            throw new BusinessException("Batch not found");
        }
        BigDecimal newAvailable = batch.getAvailableQty().add(deltaQty);
        if (newAvailable.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Insufficient inventory for batch " + batch.getBatchNo());
        }
        batch.setAvailableQty(newAvailable);
        batch.setTotalQty(batch.getTotalQty().add(deltaQty));
        this.updateById(batch);

        InventoryTxn txn = new InventoryTxn();
        txn.setBizType(bizType);
        txn.setBizId(bizId);
        txn.setBatchId(batchId);
        txn.setFruitId(batch.getFruitId());
        txn.setWarehouseId(batch.getWarehouseId());
        txn.setChangeQty(deltaQty);
        txn.setBalanceQty(newAvailable);
        txn.setOperatorId(operatorId);
        txn.setRemark("Auto generated");
        inventoryTxnMapper.insert(txn);

        // auto close low-stock alert if recovered
        resolveAlertIfRecovered(batch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InventoryBatch createOrIncreaseBatch(InventoryBatch incoming, BigDecimal inQty, String bizType, Long bizId, Long operatorId) {
        LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryBatch::getFruitId, incoming.getFruitId())
                .eq(InventoryBatch::getWarehouseId, incoming.getWarehouseId())
                .eq(InventoryBatch::getBatchNo, incoming.getBatchNo());
        InventoryBatch existing = this.getOne(wrapper);
        if (existing == null) {
            incoming.setTotalQty(inQty);
            incoming.setAvailableQty(inQty);
            incoming.setLockedQty(BigDecimal.ZERO);
            this.save(incoming);
            InventoryTxn txn = new InventoryTxn();
            txn.setBizType(bizType);
            txn.setBizId(bizId);
            txn.setBatchId(incoming.getId());
            txn.setFruitId(incoming.getFruitId());
            txn.setWarehouseId(incoming.getWarehouseId());
            txn.setChangeQty(inQty);
            txn.setBalanceQty(inQty);
            txn.setOperatorId(operatorId);
            txn.setRemark("New batch in");
            inventoryTxnMapper.insert(txn);
            return incoming;
        } else {
            adjustStock(existing.getId(), inQty, bizType, bizId, operatorId);
            return existing;
        }
    }

    private void resolveAlertIfRecovered(InventoryBatch batch) {
        LambdaQueryWrapper<InventoryAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InventoryAlert::getBatchId, batch.getId());
        wrapper.eq(InventoryAlert::getAlertStatus, "UNHANDLED");
        List<InventoryAlert> alerts = inventoryAlertMapper.selectList(wrapper);
        for (InventoryAlert alert : alerts) {
            // if low stock alert and now recovered
            if ("LOW_STOCK".equals(alert.getAlertType()) && batch.getAvailableQty().compareTo(alert.getThresholdValue()) > 0) {
                alert.setAlertStatus("HANDLED");
                inventoryAlertMapper.updateById(alert);
            }
        }
    }
}
