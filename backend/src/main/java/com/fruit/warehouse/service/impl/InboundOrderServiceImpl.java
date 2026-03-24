package com.fruit.warehouse.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.BusinessException;
import com.fruit.warehouse.dto.InboundOrderCreateDTO;
import com.fruit.warehouse.dto.InboundReceiveDTO;
import com.fruit.warehouse.entity.*;
import com.fruit.warehouse.enums.BatchStatus;
import com.fruit.warehouse.enums.OrderStatus;
import com.fruit.warehouse.mapper.*;
import com.fruit.warehouse.security.UserContext;
import com.fruit.warehouse.service.InboundOrderService;
import com.fruit.warehouse.vo.InboundOrderDetailVO;
import com.fruit.warehouse.vo.InboundOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InboundOrderServiceImpl implements InboundOrderService {

    private final InboundOrderMapper orderMapper;
    private final InboundOrderDetailMapper detailMapper;
    private final InboundRecordMapper recordMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryBatchMapper batchMapper;
    private final ProductMapper productMapper;
    private final SupplierMapper supplierMapper;
    private final WarehouseMapper warehouseMapper;
    private final SysUserMapper userMapper;

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    @Override
    public Page<InboundOrderVO> page(Integer pageNum, Integer pageSize, String orderCode, Integer status, Long supplierId, Long warehouseId) {
        Page<InboundOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(orderCode), InboundOrder::getOrderCode, orderCode)
               .eq(status != null, InboundOrder::getOrderStatus, status)
               .eq(supplierId != null, InboundOrder::getSupplierId, supplierId)
               .eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId)
               .orderByDesc(InboundOrder::getCreateTime);

        Page<InboundOrder> result = orderMapper.selectPage(page, wrapper);
        Page<InboundOrderVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public InboundOrderVO getById(Long id) {
        InboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        InboundOrderVO vo = toVO(order);
        
        // Load details
        List<InboundOrderDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<InboundOrderDetail>().eq(InboundOrderDetail::getInboundOrderId, id));
        vo.setDetails(details.stream().map(this::toDetailVO).collect(Collectors.toList()));
        
        return vo;
    }

    @Override
    @Transactional
    public Long create(InboundOrderCreateDTO dto) {
        InboundOrder order = new InboundOrder();
        order.setOrderCode(generateOrderCode());
        order.setSupplierId(dto.getSupplierId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setOrderStatus(OrderStatus.DRAFT.getCode());
        order.setExpectedDate(dto.getExpectedDate());
        order.setRemark(dto.getRemark());
        order.setCreatorId(UserContext.getUserId());
        order.setTotalAmount(BigDecimal.ZERO);
        orderMapper.insert(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (InboundOrderCreateDTO.DetailItem item : dto.getDetails()) {
                InboundOrderDetail detail = new InboundOrderDetail();
                detail.setInboundOrderId(order.getId());
                detail.setProductId(item.getProductId());
                detail.setExpectedQuantity(item.getExpectedQuantity());
                detail.setActualQuantity(BigDecimal.ZERO);
                detail.setUnitCost(item.getUnitCost());
                detail.setProductionDate(item.getProductionDate());
                detail.setRemark(item.getRemark());
                
                BigDecimal amount = item.getExpectedQuantity().multiply(item.getUnitCost());
                detail.setAmount(amount);
                totalAmount = totalAmount.add(amount);
                
                detailMapper.insert(detail);
            }
        }

        order.setTotalAmount(totalAmount);
        orderMapper.updateById(order);

        return order.getId();
    }

    @Override
    public void submit(Long id) {
        InboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.DRAFT.getCode()) {
            throw new BusinessException("只有草稿状态的入库单可以提交");
        }
        order.setOrderStatus(OrderStatus.PENDING.getCode());
        orderMapper.updateById(order);
    }

    @Override
    public void review(Long id, boolean approved, String remark) {
        InboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING.getCode()) {
            throw new BusinessException("只有待审核的入库单可以审核");
        }

        order.setOrderStatus(approved ? OrderStatus.APPROVED.getCode() : OrderStatus.CANCELLED.getCode());
        order.setReviewerId(UserContext.getUserId());
        order.setReviewTime(new Date());
        if (StrUtil.isNotBlank(remark)) {
            order.setRemark(order.getRemark() + " [审核备注]" + remark);
        }
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void receive(InboundReceiveDTO dto) {
        InboundOrder order = orderMapper.selectById(dto.getOrderId());
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.APPROVED.getCode() 
                && order.getOrderStatus() != OrderStatus.PROCESSING.getCode()) {
            throw new BusinessException("只有已审核或处理中的入库单可以收货");
        }

        order.setOrderStatus(OrderStatus.PROCESSING.getCode());

        for (InboundReceiveDTO.ReceiveItem item : dto.getItems()) {
            InboundOrderDetail detail = detailMapper.selectById(item.getDetailId());
            if (detail == null) {
                throw new BusinessException("入库明细不存在");
            }

            Product product = productMapper.selectById(detail.getProductId());
            
            // Calculate expiry date
            Date productionDate = item.getProductionDate() != null ? item.getProductionDate() : new Date();
            Date expiryDate = DateUtil.offsetDay(productionDate, product.getShelfLifeDays());

            // Generate batch
            InventoryBatch batch = new InventoryBatch();
            batch.setBatchCode(generateBatchCode());
            batch.setProductId(detail.getProductId());
            batch.setWarehouseId(order.getWarehouseId());
            batch.setSupplierId(order.getSupplierId());
            batch.setInboundOrderId(order.getId());
            batch.setQuantity(item.getActualQuantity());
            batch.setRemainingQuantity(item.getActualQuantity());
            batch.setUnitCost(detail.getUnitCost());
            batch.setProductionDate(productionDate);
            batch.setExpiryDate(expiryDate);
            batch.setBatchStatus(BatchStatus.NORMAL.getCode());
            batchMapper.insert(batch);

            // Update detail
            detail.setActualQuantity(item.getActualQuantity());
            detail.setProductionDate(productionDate);
            detail.setExpiryDate(expiryDate);
            detail.setBatchId(batch.getId());
            detail.setAmount(item.getActualQuantity().multiply(detail.getUnitCost()));
            detailMapper.updateById(detail);

            // Update or insert inventory
            Inventory inventory = inventoryMapper.selectByProductAndWarehouse(detail.getProductId(), order.getWarehouseId());
            if (inventory == null) {
                inventory = new Inventory();
                inventory.setProductId(detail.getProductId());
                inventory.setWarehouseId(order.getWarehouseId());
                inventory.setTotalQuantity(item.getActualQuantity());
                inventory.setLockedQuantity(BigDecimal.ZERO);
                inventory.setAvailableQuantity(item.getActualQuantity());
                inventoryMapper.insert(inventory);
            } else {
                inventory.setTotalQuantity(inventory.getTotalQuantity().add(item.getActualQuantity()));
                inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(item.getActualQuantity()));
                inventoryMapper.updateById(inventory);
            }

            // Create inbound record
            InboundRecord record = new InboundRecord();
            record.setInboundOrderId(order.getId());
            record.setInboundDetailId(detail.getId());
            record.setProductId(detail.getProductId());
            record.setWarehouseId(order.getWarehouseId());
            record.setBatchId(batch.getId());
            record.setQuantity(item.getActualQuantity());
            record.setOperatorId(UserContext.getUserId());
            record.setOperateTime(new Date());
            recordMapper.insert(record);
        }

        // Recalculate total amount
        List<InboundOrderDetail> allDetails = detailMapper.selectList(
                new LambdaQueryWrapper<InboundOrderDetail>().eq(InboundOrderDetail::getInboundOrderId, order.getId()));
        BigDecimal total = allDetails.stream()
                .filter(d -> d.getAmount() != null)
                .map(InboundOrderDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        orderMapper.updateById(order);
    }

    @Override
    public void complete(Long id) {
        InboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        order.setOrderStatus(OrderStatus.COMPLETED.getCode());
        order.setActualDate(new Date());
        orderMapper.updateById(order);
    }

    @Override
    public void cancel(Long id) {
        InboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getOrderStatus() == OrderStatus.COMPLETED.getCode()) {
            throw new BusinessException("已完成的入库单无法取消");
        }
        order.setOrderStatus(OrderStatus.CANCELLED.getCode());
        orderMapper.updateById(order);
    }

    private String generateOrderCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        int seq = SEQ.incrementAndGet() % 10000;
        return String.format("IN%s%04d", dateStr, seq);
    }

    private String generateBatchCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        int seq = SEQ.incrementAndGet() % 10000;
        return String.format("B%s%04d", dateStr, seq);
    }

    private InboundOrderVO toVO(InboundOrder order) {
        InboundOrderVO vo = new InboundOrderVO();
        vo.setId(order.getId());
        vo.setOrderCode(order.getOrderCode());
        vo.setSupplierId(order.getSupplierId());
        vo.setWarehouseId(order.getWarehouseId());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setOrderStatusDesc(OrderStatus.fromCode(order.getOrderStatus()).getDesc());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setExpectedDate(order.getExpectedDate());
        vo.setActualDate(order.getActualDate());
        vo.setRemark(order.getRemark());
        vo.setCreatorId(order.getCreatorId());
        vo.setReviewerId(order.getReviewerId());
        vo.setReviewTime(order.getReviewTime());
        vo.setCreateTime(order.getCreateTime());

        // Load related names
        Supplier supplier = supplierMapper.selectById(order.getSupplierId());
        if (supplier != null) vo.setSupplierName(supplier.getSupplierName());
        
        Warehouse warehouse = warehouseMapper.selectById(order.getWarehouseId());
        if (warehouse != null) vo.setWarehouseName(warehouse.getWarehouseName());

        SysUser creator = userMapper.selectById(order.getCreatorId());
        if (creator != null) vo.setCreatorName(creator.getRealName());

        if (order.getReviewerId() != null) {
            SysUser reviewer = userMapper.selectById(order.getReviewerId());
            if (reviewer != null) vo.setReviewerName(reviewer.getRealName());
        }

        return vo;
    }

    private InboundOrderDetailVO toDetailVO(InboundOrderDetail detail) {
        InboundOrderDetailVO vo = new InboundOrderDetailVO();
        vo.setId(detail.getId());
        vo.setInboundOrderId(detail.getInboundOrderId());
        vo.setProductId(detail.getProductId());
        vo.setExpectedQuantity(detail.getExpectedQuantity());
        vo.setActualQuantity(detail.getActualQuantity());
        vo.setUnitCost(detail.getUnitCost());
        vo.setAmount(detail.getAmount());
        vo.setProductionDate(detail.getProductionDate());
        vo.setExpiryDate(detail.getExpiryDate());
        vo.setBatchId(detail.getBatchId());
        vo.setRemark(detail.getRemark());

        Product product = productMapper.selectById(detail.getProductId());
        if (product != null) {
            vo.setProductName(product.getProductName());
            vo.setProductCode(product.getProductCode());
            vo.setUnit(product.getUnit());
        }

        if (detail.getBatchId() != null) {
            InventoryBatch batch = batchMapper.selectById(detail.getBatchId());
            if (batch != null) vo.setBatchCode(batch.getBatchCode());
        }

        return vo;
    }
}
