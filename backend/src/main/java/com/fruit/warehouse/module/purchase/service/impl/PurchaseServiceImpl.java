package com.fruit.warehouse.module.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import com.fruit.warehouse.module.inventory.service.InventoryService;
import com.fruit.warehouse.module.purchase.dto.PurchaseOrderCreateRequest;
import com.fruit.warehouse.module.purchase.dto.PurchaseReceiveRequest;
import com.fruit.warehouse.module.purchase.entity.PurchaseOrder;
import com.fruit.warehouse.module.purchase.entity.PurchaseOrderItem;
import com.fruit.warehouse.module.purchase.mapper.PurchaseOrderItemMapper;
import com.fruit.warehouse.module.purchase.mapper.PurchaseOrderMapper;
import com.fruit.warehouse.module.purchase.service.PurchaseService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseService {

    private final PurchaseOrderItemMapper itemMapper;
    private final InventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder createOrder(PurchaseOrderCreateRequest request) {
        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseNo("PO" + System.currentTimeMillis());
        order.setSupplierId(request.getSupplierId());
        order.setWarehouseId(request.getWarehouseId());
        order.setOrderStatus("DRAFT");
        order.setOrderDate(request.getOrderDate());
        order.setExpectedArrivalDate(request.getExpectedArrivalDate());
        order.setRemark(request.getRemark());
        BigDecimal total = BigDecimal.ZERO;
        this.save(order);

        for (PurchaseOrderCreateRequest.PurchaseItemRequest itemReq : request.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrderId(order.getId());
            item.setFruitId(itemReq.getFruitId());
            item.setBatchNo(itemReq.getBatchNo());
            item.setProductionDate(itemReq.getProductionDate());
            item.setExpirationDate(itemReq.getExpirationDate());
            item.setQuantity(itemReq.getQuantity());
            item.setReceivedQty(BigDecimal.ZERO);
            item.setUnitPrice(itemReq.getUnitPrice());
            BigDecimal subtotal = itemReq.getQuantity().multiply(itemReq.getUnitPrice());
            item.setSubtotal(subtotal);
            item.setRemark(itemReq.getRemark());
            itemMapper.insert(item);
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);
        this.updateById(order);
        return order;
    }

    @Override
    public PurchaseOrder submit(Long orderId) {
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("Purchase order not found");
        }
        if (!Objects.equals("DRAFT", order.getOrderStatus())) {
            throw new BusinessException("Only draft order can be submitted");
        }
        order.setOrderStatus("SUBMITTED");
        this.updateById(order);
        return order;
    }

    @Override
    public PurchaseOrder approve(Long orderId) {
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("Purchase order not found");
        }
        if (!Objects.equals("SUBMITTED", order.getOrderStatus())) {
            throw new BusinessException("Only submitted order can be approved");
        }
        order.setOrderStatus("APPROVED");
        this.updateById(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder receive(Long orderId, PurchaseReceiveRequest request) {
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("Purchase order not found");
        }
        if (!Objects.equals("APPROVED", order.getOrderStatus())) {
            throw new BusinessException("Only approved order can be received");
        }
        for (PurchaseReceiveRequest.ReceiveItem receiveItem : request.getItems()) {
            PurchaseOrderItem item = itemMapper.selectById(receiveItem.getItemId());
            if (item == null) {
                throw new BusinessException("Purchase item not found: " + receiveItem.getItemId());
            }
            BigDecimal newReceived = item.getReceivedQty().add(receiveItem.getReceivedQty());
            if (newReceived.compareTo(item.getQuantity()) > 0) {
                throw new BusinessException("Received qty exceeds ordered qty for item " + item.getId());
            }
            item.setReceivedQty(newReceived);
            itemMapper.updateById(item);

            InventoryBatch batch = new InventoryBatch();
            batch.setFruitId(item.getFruitId());
            batch.setWarehouseId(order.getWarehouseId());
            batch.setBatchNo(item.getBatchNo());
            batch.setSourceType("PURCHASE_IN");
            batch.setSourceId(orderId);
            batch.setProductionDate(item.getProductionDate());
            batch.setExpirationDate(item.getExpirationDate());
            batch.setUnitCost(receiveItem.getUnitCost() != null ? receiveItem.getUnitCost() : item.getUnitPrice());
            batch.setStatus("IN_STOCK");
            inventoryService.createOrIncreaseBatch(batch, receiveItem.getReceivedQty(), "PURCHASE_IN", orderId, request.getOperatorId());
        }
        order.setOrderStatus("RECEIVED");
        order.setReceivedBy(request.getOperatorId());
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);
        return order;
    }

    @Override
    public IPage<PurchaseOrder> pageList(int pageNo, int pageSize, String status, Long supplierId) {
        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, PurchaseOrder::getOrderStatus, status);
        wrapper.eq(supplierId != null, PurchaseOrder::getSupplierId, supplierId);
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);
        return this.page(new Page<>(pageNo, pageSize), wrapper);
    }
}
