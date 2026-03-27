package com.fruit.warehouse.module.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.inventory.dto.StockCheckApproveRequest;
import com.fruit.warehouse.module.inventory.dto.StockCheckCreateRequest;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import com.fruit.warehouse.module.inventory.entity.StockCheckItem;
import com.fruit.warehouse.module.inventory.entity.StockCheckOrder;
import com.fruit.warehouse.module.inventory.mapper.InventoryBatchMapper;
import com.fruit.warehouse.module.inventory.mapper.StockCheckItemMapper;
import com.fruit.warehouse.module.inventory.mapper.StockCheckOrderMapper;
import com.fruit.warehouse.module.inventory.service.InventoryService;
import com.fruit.warehouse.module.inventory.service.StockCheckService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockCheckServiceImpl extends ServiceImpl<StockCheckOrderMapper, StockCheckOrder> implements StockCheckService {

    private final StockCheckItemMapper itemMapper;
    private final InventoryBatchMapper batchMapper;
    private final InventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockCheckOrder createOrder(StockCheckCreateRequest request) {
        StockCheckOrder order = new StockCheckOrder();
        order.setCheckNo("CHK" + System.currentTimeMillis());
        order.setWarehouseId(request.getWarehouseId());
        order.setCheckStatus("DRAFT");
        order.setCheckDate(request.getCheckDate());
        order.setRemark(request.getRemark());
        this.save(order);

        for (StockCheckCreateRequest.StockCheckItemRequest itemReq : request.getItems()) {
            InventoryBatch batch = batchMapper.selectById(itemReq.getBatchId());
            if (batch == null) {
                throw new BusinessException("Batch not found for stock check");
            }
            StockCheckItem item = new StockCheckItem();
            item.setCheckOrderId(order.getId());
            item.setBatchId(batch.getId());
            item.setFruitId(batch.getFruitId());
            item.setSystemQty(batch.getAvailableQty());
            item.setActualQty(itemReq.getActualQty());
            BigDecimal diff = itemReq.getActualQty().subtract(batch.getAvailableQty());
            item.setDiffQty(diff);
            item.setReason(itemReq.getReason());
            item.setCreateTime(LocalDateTime.now());
            itemMapper.insert(item);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockCheckOrder approve(Long orderId, StockCheckApproveRequest request) {
        StockCheckOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("Stock check order not found");
        }
        if (!Objects.equals("DRAFT", order.getCheckStatus()) && !Objects.equals("SUBMITTED", order.getCheckStatus())) {
            throw new BusinessException("Only draft/submitted orders can be approved");
        }
        LambdaQueryWrapper<StockCheckItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockCheckItem::getCheckOrderId, orderId);
        List<StockCheckItem> items = itemMapper.selectList(wrapper);
        for (StockCheckItem item : items) {
            if (item.getDiffQty().compareTo(BigDecimal.ZERO) > 0) {
                inventoryService.adjustStock(item.getBatchId(), item.getDiffQty(), "STOCK_CHECK_PROFIT", orderId, request.getApprovedBy());
            } else if (item.getDiffQty().compareTo(BigDecimal.ZERO) < 0) {
                inventoryService.adjustStock(item.getBatchId(), item.getDiffQty(), "STOCK_CHECK_LOSS", orderId, request.getApprovedBy());
            }
        }
        order.setCheckStatus("APPROVED");
        order.setApprovedBy(request.getApprovedBy());
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);
        return order;
    }

    @Override
    public IPage<StockCheckOrder> pageList(int pageNo, int pageSize, Long warehouseId, String status) {
        LambdaQueryWrapper<StockCheckOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, StockCheckOrder::getWarehouseId, warehouseId);
        wrapper.eq(status != null, StockCheckOrder::getCheckStatus, status);
        wrapper.orderByDesc(StockCheckOrder::getCreateTime);
        return this.page(new Page<>(pageNo, pageSize), wrapper);
    }
}
