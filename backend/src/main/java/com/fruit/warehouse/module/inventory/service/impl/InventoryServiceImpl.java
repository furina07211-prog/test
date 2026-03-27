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

/**
 * 库存管理 模块服务实现。
 */
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryBatchMapper, InventoryBatch> implements InventoryService {

    private final InventoryTxnMapper inventoryTxnMapper;
    private final InventoryAlertMapper inventoryAlertMapper;

    /**
     * 条件分页查询库存批次。
     */
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

    /**
     * 调整批次库存并记录库存流水。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(Long batchId, BigDecimal deltaQty, String bizType, Long bizId, Long operatorId) {
        InventoryBatch batch = this.getById(batchId);
        if (batch == null) {
            throw new BusinessException("库存批次不存在");
        }
        BigDecimal newAvailable = batch.getAvailableQty().add(deltaQty);
        if (newAvailable.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("批次库存不足：" + batch.getBatchNo());
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
        txn.setRemark("系统自动生成流水");
        inventoryTxnMapper.insert(txn);

        // 低库存恢复后自动关闭对应预警
        resolveAlertIfRecovered(batch);
    }

    /**
     * 按水果+仓库+批次号创建新批次或累加现有批次库存。
     */
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
            txn.setRemark("新批次入库");
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
            // 仅处理低库存预警：当可用量恢复到阈值以上时自动关闭
            if ("LOW_STOCK".equals(alert.getAlertType()) && batch.getAvailableQty().compareTo(alert.getThresholdValue()) > 0) {
                alert.setAlertStatus("HANDLED");
                inventoryAlertMapper.updateById(alert);
            }
        }
    }
}
