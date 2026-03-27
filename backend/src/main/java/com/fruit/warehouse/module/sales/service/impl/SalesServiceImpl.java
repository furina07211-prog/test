package com.fruit.warehouse.module.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.basic.entity.Customer;
import com.fruit.warehouse.module.basic.entity.Fruit;
import com.fruit.warehouse.module.basic.entity.Warehouse;
import com.fruit.warehouse.module.basic.mapper.CustomerMapper;
import com.fruit.warehouse.module.basic.mapper.FruitMapper;
import com.fruit.warehouse.module.basic.mapper.WarehouseMapper;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import com.fruit.warehouse.module.inventory.mapper.InventoryBatchMapper;
import com.fruit.warehouse.module.inventory.service.InventoryService;
import com.fruit.warehouse.module.sales.dto.SalesOrderCreateRequest;
import com.fruit.warehouse.module.sales.dto.SalesOrderPageQuery;
import com.fruit.warehouse.module.sales.dto.SalesShipRequest;
import com.fruit.warehouse.module.sales.entity.SalesOrder;
import com.fruit.warehouse.module.sales.entity.SalesOrderItem;
import com.fruit.warehouse.module.sales.mapper.SalesOrderItemMapper;
import com.fruit.warehouse.module.sales.mapper.SalesOrderMapper;
import com.fruit.warehouse.module.sales.service.SalesService;
import com.fruit.warehouse.module.sales.vo.SalesOrderItemVO;
import com.fruit.warehouse.module.sales.vo.SalesOrderPageVO;
import java.math.BigDecimal;
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

@Service
@RequiredArgsConstructor
public class SalesServiceImpl extends ServiceImpl<SalesOrderMapper, SalesOrder> implements SalesService {

    private final SalesOrderItemMapper itemMapper;
    private final InventoryBatchMapper batchMapper;
    private final InventoryService inventoryService;
    private final CustomerMapper customerMapper;
    private final WarehouseMapper warehouseMapper;
    private final FruitMapper fruitMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SalesOrder createOrder(SalesOrderCreateRequest request) {
        validateCreateRequest(request);

        SalesOrder order = new SalesOrder();
        order.setSalesNo("SO" + System.currentTimeMillis());
        order.setCustomerId(request.getCustomerId());
        order.setWarehouseId(request.getWarehouseId());
        order.setOrderStatus("DRAFT");
        order.setOrderTime(request.getOrderTime() != null ? request.getOrderTime() : LocalDateTime.now());
        order.setRemark(request.getRemark());
        BigDecimal total = BigDecimal.ZERO;
        this.save(order);

        for (SalesOrderCreateRequest.SalesItemRequest itemReq : request.getItems()) {
            SalesOrderItem item = new SalesOrderItem();
            item.setSalesOrderId(order.getId());
            item.setFruitId(itemReq.getFruitId());
            item.setQuantity(itemReq.getQuantity());
            item.setShippedQty(BigDecimal.ZERO);
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
        SalesOrder order = requireOrder(orderId);
        if (!Objects.equals("DRAFT", order.getOrderStatus())) {
            throw new BusinessException("Only draft order can be submitted");
        }
        order.setOrderStatus("SUBMITTED");
        this.updateById(order);
        return order;
    }

    @Override
    public SalesOrder approve(Long orderId) {
        SalesOrder order = requireOrder(orderId);
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
        SalesOrder order = requireOrder(orderId);
        if (!Objects.equals("APPROVED", order.getOrderStatus())) {
            throw new BusinessException("Only approved order can be shipped");
        }
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Ship items cannot be empty");
        }

        List<SalesOrderItem> dbItems = itemMapper.selectList(
                new LambdaQueryWrapper<SalesOrderItem>().eq(SalesOrderItem::getSalesOrderId, orderId));
        if (dbItems.isEmpty()) {
            throw new BusinessException("Sales order has no items");
        }
        Map<Long, SalesOrderItem> itemMap = dbItems.stream()
                .collect(Collectors.toMap(SalesOrderItem::getId, item -> item, (a, b) -> a, LinkedHashMap::new));

        Map<Long, BigDecimal> demandByFruit = new HashMap<>();
        for (SalesShipRequest.ShipItem shipItem : request.getItems()) {
            if (shipItem == null || shipItem.getItemId() == null) {
                throw new BusinessException("Ship item is invalid");
            }
            SalesOrderItem item = itemMap.get(shipItem.getItemId());
            if (item == null) {
                throw new BusinessException("Sales item not found: " + shipItem.getItemId());
            }
            BigDecimal shipQty = nvl(shipItem.getShipQty());
            if (shipQty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Ship qty must be greater than 0");
            }
            BigDecimal pendingQty = nvl(item.getQuantity()).subtract(nvl(item.getShippedQty()));
            if (shipQty.compareTo(pendingQty) > 0) {
                throw new BusinessException("Ship qty exceeds pending qty for item " + item.getId());
            }
            demandByFruit.merge(item.getFruitId(), shipQty, BigDecimal::add);
        }

        for (Map.Entry<Long, BigDecimal> entry : demandByFruit.entrySet()) {
            BigDecimal available = sumAvailableByFruit(order.getWarehouseId(), entry.getKey());
            if (available.compareTo(entry.getValue()) < 0) {
                throw new BusinessException("Insufficient inventory for fruit " + entry.getKey());
            }
        }

        for (SalesShipRequest.ShipItem shipItem : request.getItems()) {
            SalesOrderItem item = itemMap.get(shipItem.getItemId());
            BigDecimal remain = shipItem.getShipQty();
            List<InventoryBatch> batches = batchMapper.selectList(new LambdaQueryWrapper<InventoryBatch>()
                    .eq(InventoryBatch::getFruitId, item.getFruitId())
                    .eq(InventoryBatch::getWarehouseId, order.getWarehouseId())
                    .gt(InventoryBatch::getAvailableQty, BigDecimal.ZERO)
                    .orderByAsc(InventoryBatch::getExpirationDate)
                    .orderByAsc(InventoryBatch::getId));
            for (InventoryBatch batch : batches) {
                if (remain.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                BigDecimal available = nvl(batch.getAvailableQty());
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

            item.setShippedQty(nvl(item.getShippedQty()).add(shipItem.getShipQty()));
            itemMapper.updateById(item);
        }

        List<SalesOrderItem> finalItems = itemMapper.selectList(
                new LambdaQueryWrapper<SalesOrderItem>().eq(SalesOrderItem::getSalesOrderId, orderId));
        boolean allShipped = finalItems.stream()
                .allMatch(item -> nvl(item.getShippedQty()).compareTo(nvl(item.getQuantity())) >= 0);
        order.setOrderStatus(allShipped ? "SHIPPED" : "APPROVED");
        order.setUpdateTime(LocalDateTime.now());
        this.updateById(order);
        return order;
    }

    @Override
    public IPage<SalesOrderPageVO> pageList(SalesOrderPageQuery query) {
        SalesOrderPageQuery safeQuery = query != null ? query : new SalesOrderPageQuery();
        int pageNo = safeQuery.getPageNo() == null || safeQuery.getPageNo() < 1 ? 1 : safeQuery.getPageNo();
        int pageSize = safeQuery.getPageSize() == null || safeQuery.getPageSize() < 1 ? 10 : safeQuery.getPageSize();

        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(safeQuery.getSalesNo()), SalesOrder::getSalesNo, safeQuery.getSalesNo());
        wrapper.eq(StringUtils.hasText(safeQuery.getStatus()), SalesOrder::getOrderStatus, safeQuery.getStatus());
        wrapper.eq(safeQuery.getCustomerId() != null, SalesOrder::getCustomerId, safeQuery.getCustomerId());
        wrapper.eq(safeQuery.getWarehouseId() != null, SalesOrder::getWarehouseId, safeQuery.getWarehouseId());
        wrapper.orderByDesc(SalesOrder::getCreateTime);

        IPage<SalesOrder> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        List<SalesOrder> orders = page.getRecords();
        if (orders == null || orders.isEmpty()) {
            Page<SalesOrderPageVO> emptyPage = new Page<>(pageNo, pageSize, page.getTotal());
            emptyPage.setRecords(Collections.emptyList());
            return emptyPage;
        }

        Set<Long> customerIds = orders.stream().map(SalesOrder::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> warehouseIds = orders.stream().map(SalesOrder::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> orderIds = orders.stream().map(SalesOrder::getId).collect(Collectors.toSet());

        Map<Long, String> customerNameMap = customerIds.isEmpty()
                ? Collections.emptyMap()
                : customerMapper.selectBatchIds(customerIds).stream()
                .collect(Collectors.toMap(Customer::getId, Customer::getCustomerName));

        Map<Long, String> warehouseNameMap = warehouseIds.isEmpty()
                ? Collections.emptyMap()
                : warehouseMapper.selectBatchIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName));

        List<SalesOrderItem> items = orderIds.isEmpty()
                ? Collections.emptyList()
                : itemMapper.selectList(new LambdaQueryWrapper<SalesOrderItem>()
                .in(SalesOrderItem::getSalesOrderId, orderIds));

        Map<Long, BigDecimal> totalQtyMap = new HashMap<>();
        Map<Long, BigDecimal> shippedQtyMap = new HashMap<>();
        for (SalesOrderItem item : items) {
            Long soId = item.getSalesOrderId();
            totalQtyMap.merge(soId, nvl(item.getQuantity()), BigDecimal::add);
            shippedQtyMap.merge(soId, nvl(item.getShippedQty()), BigDecimal::add);
        }

        List<SalesOrderPageVO> records = new ArrayList<>(orders.size());
        for (SalesOrder order : orders) {
            SalesOrderPageVO vo = new SalesOrderPageVO();
            vo.setId(order.getId());
            vo.setSalesNo(order.getSalesNo());
            vo.setCustomerId(order.getCustomerId());
            vo.setCustomerName(customerNameMap.get(order.getCustomerId()));
            vo.setWarehouseId(order.getWarehouseId());
            vo.setWarehouseName(warehouseNameMap.get(order.getWarehouseId()));
            vo.setOrderStatus(order.getOrderStatus());
            vo.setOrderTime(order.getOrderTime());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setCreatedBy(order.getCreatedBy());
            vo.setRemark(order.getRemark());
            vo.setCreateTime(order.getCreateTime());
            vo.setUpdateTime(order.getUpdateTime());
            BigDecimal totalQty = nvl(totalQtyMap.get(order.getId()));
            BigDecimal shippedQty = nvl(shippedQtyMap.get(order.getId()));
            vo.setTotalQty(totalQty);
            vo.setShippedQty(shippedQty);
            vo.setPendingQty(totalQty.subtract(shippedQty).max(BigDecimal.ZERO));
            records.add(vo);
        }

        Page<SalesOrderPageVO> result = new Page<>(pageNo, pageSize, page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public List<SalesOrderItemVO> listItems(Long orderId) {
        requireOrder(orderId);
        List<SalesOrderItem> items = itemMapper.selectList(new LambdaQueryWrapper<SalesOrderItem>()
                .eq(SalesOrderItem::getSalesOrderId, orderId)
                .orderByAsc(SalesOrderItem::getId));
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> fruitIds = items.stream().map(SalesOrderItem::getFruitId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> fruitNameMap = fruitIds.isEmpty()
                ? Collections.emptyMap()
                : fruitMapper.selectBatchIds(fruitIds).stream().collect(Collectors.toMap(Fruit::getId, Fruit::getFruitName));

        List<SalesOrderItemVO> result = new ArrayList<>(items.size());
        for (SalesOrderItem item : items) {
            SalesOrderItemVO vo = new SalesOrderItemVO();
            vo.setId(item.getId());
            vo.setSalesOrderId(item.getSalesOrderId());
            vo.setFruitId(item.getFruitId());
            vo.setFruitName(fruitNameMap.get(item.getFruitId()));
            vo.setBatchId(item.getBatchId());
            vo.setQuantity(item.getQuantity());
            vo.setShippedQty(nvl(item.getShippedQty()));
            vo.setPendingQty(nvl(item.getQuantity()).subtract(nvl(item.getShippedQty())).max(BigDecimal.ZERO));
            vo.setUnitPrice(item.getUnitPrice());
            vo.setSubtotal(item.getSubtotal());
            vo.setRemark(item.getRemark());
            vo.setCreateTime(item.getCreateTime());
            vo.setUpdateTime(item.getUpdateTime());
            result.add(vo);
        }
        return result;
    }

    private BigDecimal sumAvailableByFruit(Long warehouseId, Long fruitId) {
        List<InventoryBatch> batches = batchMapper.selectList(new LambdaQueryWrapper<InventoryBatch>()
                .eq(InventoryBatch::getFruitId, fruitId)
                .eq(InventoryBatch::getWarehouseId, warehouseId)
                .gt(InventoryBatch::getAvailableQty, BigDecimal.ZERO));
        BigDecimal total = BigDecimal.ZERO;
        for (InventoryBatch batch : batches) {
            total = total.add(nvl(batch.getAvailableQty()));
        }
        return total;
    }

    private SalesOrder requireOrder(Long orderId) {
        SalesOrder order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException("Sales order not found");
        }
        return order;
    }

    private void validateCreateRequest(SalesOrderCreateRequest request) {
        if (request == null) {
            throw new BusinessException("Request body cannot be empty");
        }
        if (request.getCustomerId() == null) {
            throw new BusinessException("Customer is required");
        }
        if (request.getWarehouseId() == null) {
            throw new BusinessException("Warehouse is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("At least one sales item is required");
        }
        for (SalesOrderCreateRequest.SalesItemRequest item : request.getItems()) {
            if (item.getFruitId() == null) {
                throw new BusinessException("Fruit is required for each item");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Quantity must be greater than 0");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException("Unit price cannot be negative");
            }
        }
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
