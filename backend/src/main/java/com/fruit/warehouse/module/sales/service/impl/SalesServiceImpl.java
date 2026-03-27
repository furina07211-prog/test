package com.fruit.warehouse.module.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import com.fruit.warehouse.module.inventory.mapper.InventoryBatchMapper;
import com.fruit.warehouse.module.inventory.service.InventoryService;
import com.fruit.warehouse.module.sales.dto.SalesOrderCreateRequest;
import com.fruit.warehouse.module.sales.dto.SalesShipRequest;
import com.fruit.warehouse.module.sales.entity.SalesOrder;
import com.fruit.warehouse.module.sales.entity.SalesOrderItem;
import com.fruit.warehouse.module.sales.mapper.SalesOrderItemMapper;
import com.fruit.warehouse.module.sales.mapper.SalesOrderMapper;
import com.fruit.warehouse.module.sales.service.SalesService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SalesServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesService {

    private final SalesOrderItemMapper itemMapper;
    private final InventoryBatchMapper batchMapper;
    private final InventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalesOrder createOrder(SalesOrderCreateRequest request) {
        SalesOrder order = new SalesOrder();
        order.setSalesNo("SO" + System.currentTimeMillis());
        order.setCustomerId(request.getCustomerId());
        order.setWarehouseId(request.getWarehouseId());
        order.setOrderStatus("DRAFT");
        order.setOrderTime(request.getOrderTime());
        order.setRemark(request.getRemark());
        BigDecimal total = BigDecimal.ZERO;
        this.save(order);

        for (SalesOrderCreateRequest.SalesItemRequest itemReq : request.getItems()) {
            SalesOrderItem item = new SalesOrderItem();
            item.setSalesOrderId(order.getId());
            item.setFruitId(itemReq.getFruitId());
            item.setQuantity(itemReq.getQuantity());
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
    public SalesOrder submit(Long orderId) {
        SalesOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("Sales order not found");
        }
        if (!Objects.equals("DRAFT", order.getOrderStatus())) {
            throw new BusinessException("Only draft order can be submitted");
        }
        order.setOrderStatus("SUBMITTED");
        this.updateById(order);
        return order;
    }

    @Override
    public SalesOrder approve(Long orderId) {
        SalesOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("Sales order not found");
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
    public SalesOrder ship(Long orderId, SalesShipRequest request) {
        SalesOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("Sales order not found");
        }
        if (!Objects.equals("APPROVED", order.getOrderStatus())) {
            throw new BusinessException("Only approved order can be shipped");
        }
        LambdaQueryWrapper<SalesOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(SalesOrderItem::getSalesOrderId, orderId);
        List<SalesOrderItem> items = itemMapper.selectList(itemWrapper);
        for (SalesOrderItem item : items) {
            BigDecimal remain = item.getQuantity();
            LambdaQueryWrapper<InventoryBatch> batchWrapper = new LambdaQueryWrapper<>();
            batchWrapper.eq(InventoryBatch::getFruitId, item.getFruitId())
                    .eq(InventoryBatch::getWarehouseId, order.getWarehouseId())
                    .gt(InventoryBatch::getAvailableQty, BigDecimal.ZERO)
                    .orderByAsc(InventoryBatch::getExpirationDate);
            List<InventoryBatch> batches = batchMapper.selectList(batchWrapper);
            for (InventoryBatch batch : batches) {
                if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                BigDecimal available = batch.getAvailableQty();
                if (available.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                BigDecimal pick = available.min(remain);
                inventoryService.adjustStock(batch.getId(), pick.negate(), "SALES_OUT", orderId, request.getOperatorId());
                remain = remain.subtract(pick);
            }
            if (remain.compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessException("Insufficient inventory for fruit " + item.getFruitId());
            }
        }
        order.setOrderStatus("SHIPPED");
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);
        return order;
    }

    @Override
    public IPage<SalesOrder> pageList(int pageNo, int pageSize, String status, Long customerId) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SalesOrder::getOrderStatus, status);
        wrapper.eq(customerId != null, SalesOrder::getCustomerId, customerId);
        wrapper.orderByDesc(SalesOrder::getCreateTime);
        return this.page(new Page<>(pageNo, pageSize), wrapper);
    }
}
