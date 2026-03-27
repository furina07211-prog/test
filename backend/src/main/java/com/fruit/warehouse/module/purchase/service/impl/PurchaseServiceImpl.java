package com.fruit.warehouse.module.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.basic.entity.Fruit;
import com.fruit.warehouse.module.basic.entity.Supplier;
import com.fruit.warehouse.module.basic.entity.Warehouse;
import com.fruit.warehouse.module.basic.mapper.FruitMapper;
import com.fruit.warehouse.module.basic.mapper.SupplierMapper;
import com.fruit.warehouse.module.basic.mapper.WarehouseMapper;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import com.fruit.warehouse.module.inventory.service.InventoryService;
import com.fruit.warehouse.module.purchase.dto.PurchaseOrderCreateRequest;
import com.fruit.warehouse.module.purchase.dto.PurchaseOrderPageQuery;
import com.fruit.warehouse.module.purchase.dto.PurchaseReceiveRequest;
import com.fruit.warehouse.module.purchase.entity.PurchaseOrder;
import com.fruit.warehouse.module.purchase.entity.PurchaseOrderItem;
import com.fruit.warehouse.module.purchase.mapper.PurchaseOrderItemMapper;
import com.fruit.warehouse.module.purchase.mapper.PurchaseOrderMapper;
import com.fruit.warehouse.module.purchase.service.PurchaseService;
import com.fruit.warehouse.module.purchase.vo.PurchaseOrderItemVO;
import com.fruit.warehouse.module.purchase.vo.PurchaseOrderPageVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 采购管理 模块服务实现。
 */
@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseService {

    private final PurchaseOrderItemMapper itemMapper;
    private final SupplierMapper supplierMapper;
    private final WarehouseMapper warehouseMapper;
    private final FruitMapper fruitMapper;
    private final InventoryService inventoryService;

    /**
     * 创建采购单：写入主单、明细并汇总金额，初始状态为草稿。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder createOrder(PurchaseOrderCreateRequest request) {
        validateCreateRequest(request);

        PurchaseOrder order = new PurchaseOrder();
        order.setPurchaseNo("PO" + System.currentTimeMillis());
        order.setSupplierId(request.getSupplierId());
        order.setWarehouseId(request.getWarehouseId());
        order.setOrderStatus("DRAFT");
        order.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now());
        order.setExpectedArrivalDate(request.getExpectedArrivalDate());
        order.setRemark(request.getRemark());
        BigDecimal total = BigDecimal.ZERO;
        this.save(order);

        for (PurchaseOrderCreateRequest.PurchaseItemRequest itemReq : request.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrderId(order.getId());
            item.setFruitId(itemReq.getFruitId());
            item.setBatchNo(itemReq.getBatchNo().trim());
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
        PurchaseOrder order = requireOrder(orderId);
        if (!Objects.equals("DRAFT", order.getOrderStatus())) {
            throw new BusinessException("仅草稿单可提交");
        }
        order.setOrderStatus("SUBMITTED");
        this.updateById(order);
        return order;
    }

    @Override
    public PurchaseOrder approve(Long orderId) {
        PurchaseOrder order = requireOrder(orderId);
        if (!Objects.equals("SUBMITTED", order.getOrderStatus())) {
            throw new BusinessException("仅已提交单据可审核");
        }
        order.setOrderStatus("APPROVED");
        this.updateById(order);
        return order;
    }

    /**
     * 分批收货：校验本次收货数量后入库，全部收齐则更新为已入库状态。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder receive(Long orderId, PurchaseReceiveRequest request) {
        PurchaseOrder order = requireOrder(orderId);
        if (!Objects.equals("APPROVED", order.getOrderStatus())) {
            throw new BusinessException("仅已审核单据可收货");
        }
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("收货明细不能为空");
        }

        List<PurchaseOrderItem> dbItems = itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getPurchaseOrderId, orderId));
        if (dbItems.isEmpty()) {
            throw new BusinessException("采购单无明细数据");
        }
        Map<Long, PurchaseOrderItem> itemMap = dbItems.stream()
                .collect(Collectors.toMap(PurchaseOrderItem::getId, item -> item, (a, b) -> a, LinkedHashMap::new));

        for (PurchaseReceiveRequest.ReceiveItem receiveItem : request.getItems()) {
            if (receiveItem == null || receiveItem.getItemId() == null) {
                throw new BusinessException("收货明细参数无效");
            }
            PurchaseOrderItem item = itemMap.get(receiveItem.getItemId());
            if (item == null) {
                throw new BusinessException("采购明细不存在：" + receiveItem.getItemId());
            }
            BigDecimal receiveQty = nvl(receiveItem.resolveReceiveQty());
            if (receiveQty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("收货数量必须大于0");
            }

            BigDecimal currentReceived = nvl(item.getReceivedQty());
            BigDecimal pendingQty = nvl(item.getQuantity()).subtract(currentReceived);
            if (receiveQty.compareTo(pendingQty) > 0) {
                throw new BusinessException("收货数量超过待收数量，明细ID：" + item.getId());
            }

            BigDecimal newReceived = currentReceived.add(receiveQty);
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
            inventoryService.createOrIncreaseBatch(batch, receiveQty, "PURCHASE_IN", orderId, request.getOperatorId());
        }

        List<PurchaseOrderItem> finalItems = itemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getPurchaseOrderId, orderId));
        boolean allReceived = finalItems.stream()
                .allMatch(item -> nvl(item.getReceivedQty()).compareTo(nvl(item.getQuantity())) >= 0);
        if (allReceived) {
            order.setOrderStatus("RECEIVED");
        } else {
            order.setOrderStatus("APPROVED");
        }
        order.setReceivedBy(request.getOperatorId());
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);
        return order;
    }

    /**
     * 分页查询采购单，并补齐供应商/仓库名称与收货进度字段。
     */
    @Override
    public IPage<PurchaseOrderPageVO> pageList(PurchaseOrderPageQuery query) {
        PurchaseOrderPageQuery safeQuery = query != null ? query : new PurchaseOrderPageQuery();
        int pageNo = safeQuery.getPageNo() == null || safeQuery.getPageNo() < 1 ? 1 : safeQuery.getPageNo();
        int pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() < 1 ? 10 : safeQuery.getPageSize();

        LambdaQueryWrapper<PurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(safeQuery.getPurchaseNo()), PurchaseOrder::getPurchaseNo, safeQuery.getPurchaseNo());
        wrapper.eq(StringUtils.hasText(safeQuery.getStatus()), PurchaseOrder::getOrderStatus, safeQuery.getStatus());
        wrapper.eq(safeQuery.getSupplierId() != null, PurchaseOrder::getSupplierId, safeQuery.getSupplierId());
        wrapper.eq(safeQuery.getWarehouseId() != null, PurchaseOrder::getWarehouseId, safeQuery.getWarehouseId());
        wrapper.orderByDesc(PurchaseOrder::getCreateTime);

        IPage<PurchaseOrder> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        List<PurchaseOrder> orders = page.getRecords();
        if (orders == null || orders.isEmpty()) {
            Page<PurchaseOrderPageVO> emptyPage = new Page<>(pageNo, pageSize, page.getTotal());
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        Set<Long> supplierIds = orders.stream().map(PurchaseOrder::getSupplierId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> warehouseIds = orders.stream().map(PurchaseOrder::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> orderIds = orders.stream().map(PurchaseOrder::getId).collect(Collectors.toSet());

        Map<Long, String> supplierNameMap = supplierIds.isEmpty()
                ? Collections.emptyMap()
                : supplierMapper.selectBatchIds(supplierIds).stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getSupplierName));

        Map<Long, String> warehouseNameMap = warehouseIds.isEmpty()
                ? Collections.emptyMap()
                : warehouseMapper.selectBatchIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));

        List<PurchaseOrderItem> items = orderIds.isEmpty()
                ? Collections.emptyList()
                : itemMapper.selectList(new LambdaQueryWrapper<PurchaseOrderItem>()
                .in(PurchaseOrderItem::getPurchaseOrderId, orderIds));

        Map<Long, BigDecimal> totalQtyMap = new HashMap<>();
        Map<Long, BigDecimal> receivedQtyMap = new HashMap<>();
        for (PurchaseOrderItem item : items) {
            Long poId = item.getPurchaseOrderId();
            totalQtyMap.merge(poId, nvl(item.getQuantity()), BigDecimal::add);
            receivedQtyMap.merge(poId, nvl(item.getReceivedQty()), BigDecimal::add);
        }

        List<PurchaseOrderPageVO> records = new ArrayList<>(orders.size());
        for (PurchaseOrder order : orders) {
            PurchaseOrderPageVO vo = new PurchaseOrderPageVO();
            vo.setId(order.getId());
            vo.setPurchaseNo(order.getPurchaseNo());
            vo.setSupplierId(order.getSupplierId());
            vo.setSupplierName(supplierNameMap.get(order.getSupplierId()));
            vo.setWarehouseId(order.getWarehouseId());
            vo.setWarehouseName(warehouseNameMap.get(order.getWarehouseId()));
            vo.setOrderStatus(order.getOrderStatus());
            vo.setOrderDate(order.getOrderDate());
            vo.setExpectedArrivalDate(order.getExpectedArrivalDate());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setCreatedBy(order.getCreatedBy());
            vo.setReceivedBy(order.getReceivedBy());
            vo.setRemark(order.getRemark());
            vo.setCreateTime(order.getCreateTime());
            vo.setUpdateTime(order.getUpdateTime());
            BigDecimal totalQty = nvl(totalQtyMap.get(order.getId()));
            BigDecimal receivedQty = nvl(receivedQtyMap.get(order.getId()));
            vo.setTotalQty(totalQty);
            vo.setReceivedQty(receivedQty);
            vo.setPendingQty(totalQty.subtract(receivedQty).max(BigDecimal.ZERO));
            records.add(vo);
        }

        Page<PurchaseOrderPageVO> result = new Page<>(pageNo, pageSize, page.getTotal());
        result.setRecords(records);
        return result;
    }

    /**
     * 查询采购单明细并计算待收数量。
     */
    @Override
    public List<PurchaseOrderItemVO> listItems(Long orderId) {
        requireOrder(orderId);
        List<PurchaseOrderItem> items = itemMapper.selectList(new LambdaQueryWrapper<PurchaseOrderItem>()
                .eq(PurchaseOrderItem::getPurchaseOrderId, orderId)
                .orderByAsc(PurchaseOrderItem::getId));
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> fruitIds = items.stream().map(PurchaseOrderItem::getFruitId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> fruitNameMap = fruitIds.isEmpty()
                ? Collections.emptyMap()
                : fruitMapper.selectBatchIds(fruitIds).stream().collect(Collectors.toMap(Fruit::getId, Fruit::getFruitName));

        List<PurchaseOrderItemVO> result = new ArrayList<>(items.size());
        for (PurchaseOrderItem item : items) {
            PurchaseOrderItemVO vo = new PurchaseOrderItemVO();
            vo.setId(item.getId());
            vo.setPurchaseOrderId(item.getPurchaseOrderId());
            vo.setFruitId(item.getFruitId());
            vo.setFruitName(fruitNameMap.get(item.getFruitId()));
            vo.setBatchNo(item.getBatchNo());
            vo.setProductionDate(item.getProductionDate());
            vo.setExpirationDate(item.getExpirationDate());
            vo.setQuantity(item.getQuantity());
            vo.setReceivedQty(nvl(item.getReceivedQty()));
            vo.setPendingQty(nvl(item.getQuantity()).subtract(nvl(item.getReceivedQty())).max(BigDecimal.ZERO));
            vo.setUnitPrice(item.getUnitPrice());
            vo.setSubtotal(item.getSubtotal());
            vo.setRemark(item.getRemark());
            vo.setCreateTime(item.getCreateTime());
            vo.setUpdateTime(item.getUpdateTime());
            result.add(vo);
        }
        return result;
    }

    private PurchaseOrder requireOrder(Long orderId) {
        PurchaseOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        return order;
    }

    /**
     * 采购单创建参数校验。
     */
    private void validateCreateRequest(PurchaseOrderCreateRequest request) {
        if (request == null) {
            throw new BusinessException("请求体不能为空");
        }
        if (request.getSupplierId() == null) {
            throw new BusinessException("供应商不能为空");
        }
        if (request.getWarehouseId() == null) {
            throw new BusinessException("仓库不能为空");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("至少需要一条采购明细");
        }
        for (PurchaseOrderCreateRequest.PurchaseItemRequest item : request.getItems()) {
            if (item.getFruitId() == null) {
                throw new BusinessException("每条明细都必须选择水果");
            }
            if (!StringUtils.hasText(item.getBatchNo())) {
                throw new BusinessException("每条明细都必须填写批次号");
            }
            if (item.getExpirationDate() == null) {
                throw new BusinessException("每条明细都必须填写到期日期");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("数量必须大于0");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("单价不能为负数");
            }
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
