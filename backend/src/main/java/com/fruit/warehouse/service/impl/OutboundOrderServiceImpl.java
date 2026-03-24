package com.fruit.warehouse.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.BusinessException;
import com.fruit.warehouse.dto.OutboundOrderCreateDTO;
import com.fruit.warehouse.entity.*;
import com.fruit.warehouse.enums.BatchStatus;
import com.fruit.warehouse.enums.OrderStatus;
import com.fruit.warehouse.mapper.*;
import com.fruit.warehouse.security.UserContext;
import com.fruit.warehouse.service.OutboundOrderService;
import com.fruit.warehouse.vo.OutboundOrderDetailVO;
import com.fruit.warehouse.vo.OutboundOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OutboundOrderServiceImpl implements OutboundOrderService {

    private final OutboundOrderMapper orderMapper;
    private final OutboundOrderDetailMapper detailMapper;
    private final OutboundRecordMapper recordMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryBatchMapper batchMapper;
    private final ProductMapper productMapper;
    private final WarehouseMapper warehouseMapper;
    private final SysUserMapper userMapper;

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    @Override
    public Page<OutboundOrderVO> page(Integer pageNum, Integer pageSize, String orderCode, Integer status, Long warehouseId) {
        Page<OutboundOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(orderCode), OutboundOrder::getOrderCode, orderCode)
               .eq(status != null, OutboundOrder::getOrderStatus, status)
               .eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId)
               .orderByDesc(OutboundOrder::getCreateTime);

        Page<OutboundOrder> result = orderMapper.selectPage(page, wrapper);
        Page<OutboundOrderVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public OutboundOrderVO getById(Long id) {
        OutboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        OutboundOrderVO vo = toVO(order);

        List<OutboundOrderDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderDetail>().eq(OutboundOrderDetail::getOutboundOrderId, id));
        vo.setDetails(details.stream().map(this::toDetailVO).collect(Collectors.toList()));

        return vo;
    }

    @Override
    @Transactional
    public Long create(OutboundOrderCreateDTO dto) {
        OutboundOrder order = new OutboundOrder();
        order.setOrderCode(generateOrderCode());
        order.setWarehouseId(dto.getWarehouseId());
        order.setOrderStatus(OrderStatus.DRAFT.getCode());
        order.setOrderType(dto.getOrderType() != null ? dto.getOrderType() : 1);
        order.setCustomerName(dto.getCustomerName());
        order.setCustomerPhone(dto.getCustomerPhone());
        order.setExpectedDate(dto.getExpectedDate());
        order.setRemark(dto.getRemark());
        order.setCreatorId(UserContext.getUserId());
        order.setTotalAmount(BigDecimal.ZERO);
        orderMapper.insert(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (dto.getDetails() != null) {
            for (OutboundOrderCreateDTO.DetailItem item : dto.getDetails()) {
                OutboundOrderDetail detail = new OutboundOrderDetail();
                detail.setOutboundOrderId(order.getId());
                detail.setProductId(item.getProductId());
                detail.setQuantity(item.getQuantity());
                detail.setActualQuantity(BigDecimal.ZERO);
                detail.setUnitPrice(item.getUnitPrice());
                detail.setRemark(item.getRemark());

                BigDecimal amount = item.getQuantity().multiply(item.getUnitPrice());
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
        OutboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.DRAFT.getCode()) {
            throw new BusinessException("只有草稿状态的出库单可以提交");
        }
        order.setOrderStatus(OrderStatus.PENDING.getCode());
        orderMapper.updateById(order);
    }

    @Override
    public void review(Long id, boolean approved, String remark) {
        OutboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.PENDING.getCode()) {
            throw new BusinessException("只有待审核的出库单可以审核");
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
    public void pick(Long id) {
        OutboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getOrderStatus() != OrderStatus.APPROVED.getCode() 
                && order.getOrderStatus() != OrderStatus.PROCESSING.getCode()) {
            throw new BusinessException("只有已审核或处理中的出库单可以拣货");
        }

        order.setOrderStatus(OrderStatus.PROCESSING.getCode());

        List<OutboundOrderDetail> details = detailMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderDetail>().eq(OutboundOrderDetail::getOutboundOrderId, id));

        for (OutboundOrderDetail detail : details) {
            BigDecimal remainingNeed = detail.getQuantity();

            // FEFO: First Expiry First Out - order by expiry_date ASC
            List<InventoryBatch> batches = batchMapper.selectAvailableBatchesFEFO(
                    detail.getProductId(), order.getWarehouseId());

            if (batches.isEmpty()) {
                throw new BusinessException("商品库存不足: " + detail.getProductId());
            }

            BigDecimal totalPicked = BigDecimal.ZERO;

            for (InventoryBatch batch : batches) {
                if (remainingNeed.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                BigDecimal deduct = batch.getRemainingQuantity().min(remainingNeed);

                // Update batch
                batch.setRemainingQuantity(batch.getRemainingQuantity().subtract(deduct));
                if (batch.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                    batch.setBatchStatus(BatchStatus.DEPLETED.getCode());
                }
                batchMapper.updateById(batch);

                // Create outbound record
                OutboundRecord record = new OutboundRecord();
                record.setOutboundOrderId(order.getId());
                record.setOutboundDetailId(detail.getId());
                record.setProductId(detail.getProductId());
                record.setWarehouseId(order.getWarehouseId());
                record.setBatchId(batch.getId());
                record.setQuantity(deduct);
                record.setOperatorId(UserContext.getUserId());
                record.setOperateTime(new Date());
                recordMapper.insert(record);

                remainingNeed = remainingNeed.subtract(deduct);
                totalPicked = totalPicked.add(deduct);
            }

            if (remainingNeed.compareTo(BigDecimal.ZERO) > 0) {
                Product product = productMapper.selectById(detail.getProductId());
                throw new BusinessException("商品【" + product.getProductName() + "】库存不足，缺少: " + remainingNeed);
            }

            // Update detail
            detail.setActualQuantity(totalPicked);
            detail.setAmount(totalPicked.multiply(detail.getUnitPrice()));
            detailMapper.updateById(detail);

            // Update inventory
            Inventory inventory = inventoryMapper.selectByProductAndWarehouse(detail.getProductId(), order.getWarehouseId());
            if (inventory != null) {
                inventory.setTotalQuantity(inventory.getTotalQuantity().subtract(totalPicked));
                inventory.setAvailableQuantity(inventory.getAvailableQuantity().subtract(totalPicked));
                inventoryMapper.updateById(inventory);
            }
        }

        // Recalculate total amount
        List<OutboundOrderDetail> allDetails = detailMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderDetail>().eq(OutboundOrderDetail::getOutboundOrderId, order.getId()));
        BigDecimal total = allDetails.stream()
                .filter(d -> d.getAmount() != null)
                .map(OutboundOrderDetail::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
        orderMapper.updateById(order);
    }

    @Override
    public void complete(Long id) {
        OutboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        order.setOrderStatus(OrderStatus.COMPLETED.getCode());
        order.setActualDate(new Date());
        orderMapper.updateById(order);
    }

    @Override
    public void cancel(Long id) {
        OutboundOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getOrderStatus() == OrderStatus.COMPLETED.getCode()) {
            throw new BusinessException("已完成的出库单无法取消");
        }
        order.setOrderStatus(OrderStatus.CANCELLED.getCode());
        orderMapper.updateById(order);
    }

    private String generateOrderCode() {
        String dateStr = DateUtil.format(new Date(), "yyyyMMdd");
        int seq = SEQ.incrementAndGet() % 10000;
        return String.format("OUT%s%04d", dateStr, seq);
    }

    private OutboundOrderVO toVO(OutboundOrder order) {
        OutboundOrderVO vo = new OutboundOrderVO();
        vo.setId(order.getId());
        vo.setOrderCode(order.getOrderCode());
        vo.setWarehouseId(order.getWarehouseId());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setOrderStatusDesc(OrderStatus.fromCode(order.getOrderStatus()).getDesc());
        vo.setOrderType(order.getOrderType());
        vo.setCustomerName(order.getCustomerName());
        vo.setCustomerPhone(order.getCustomerPhone());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setExpectedDate(order.getExpectedDate());
        vo.setActualDate(order.getActualDate());
        vo.setRemark(order.getRemark());
        vo.setCreatorId(order.getCreatorId());
        vo.setCreateTime(order.getCreateTime());

        Warehouse warehouse = warehouseMapper.selectById(order.getWarehouseId());
        if (warehouse != null) vo.setWarehouseName(warehouse.getWarehouseName());

        SysUser creator = userMapper.selectById(order.getCreatorId());
        if (creator != null) vo.setCreatorName(creator.getRealName());

        return vo;
    }

    private OutboundOrderDetailVO toDetailVO(OutboundOrderDetail detail) {
        OutboundOrderDetailVO vo = new OutboundOrderDetailVO();
        vo.setId(detail.getId());
        vo.setOutboundOrderId(detail.getOutboundOrderId());
        vo.setProductId(detail.getProductId());
        vo.setQuantity(detail.getQuantity());
        vo.setActualQuantity(detail.getActualQuantity());
        vo.setUnitPrice(detail.getUnitPrice());
        vo.setAmount(detail.getAmount());
        vo.setRemark(detail.getRemark());

        Product product = productMapper.selectById(detail.getProductId());
        if (product != null) {
            vo.setProductName(product.getProductName());
            vo.setProductCode(product.getProductCode());
            vo.setUnit(product.getUnit());
        }

        return vo;
    }
}
