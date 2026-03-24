package com.fruit.warehouse.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.warehouse.entity.*;
import com.fruit.warehouse.mapper.*;
import com.fruit.warehouse.service.StatisticsService;
import com.fruit.warehouse.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ProductMapper productMapper;
    private final SupplierMapper supplierMapper;
    private final WarehouseMapper warehouseMapper;
    private final InventoryMapper inventoryMapper;
    private final InboundOrderMapper inboundOrderMapper;
    private final OutboundOrderMapper outboundOrderMapper;
    private final InboundRecordMapper inboundRecordMapper;
    private final OutboundRecordMapper outboundRecordMapper;
    private final AlertMapper alertMapper;
    private final InventorySnapshotMapper snapshotMapper;

    @Override
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        
        // Total counts
        vo.setTotalProducts(productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)).intValue());
        vo.setTotalSuppliers(supplierMapper.selectCount(new LambdaQueryWrapper<Supplier>().eq(Supplier::getStatus, 1)).intValue());
        vo.setTotalWarehouses(warehouseMapper.selectCount(new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getStatus, 1)).intValue());
        
        // Total inventory
        List<Inventory> inventories = inventoryMapper.selectList(null);
        BigDecimal totalQty = inventories.stream()
                .map(Inventory::getTotalQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalInventory(totalQty);
        
        // Today's orders
        Date todayStart = DateUtil.beginOfDay(new Date());
        Date todayEnd = DateUtil.endOfDay(new Date());
        
        vo.setTodayInboundCount(inboundOrderMapper.selectCount(
                new LambdaQueryWrapper<InboundOrder>()
                        .between(InboundOrder::getCreateTime, todayStart, todayEnd)).intValue());
        
        vo.setTodayOutboundCount(outboundOrderMapper.selectCount(
                new LambdaQueryWrapper<OutboundOrder>()
                        .between(OutboundOrder::getCreateTime, todayStart, todayEnd)).intValue());
        
        // Active alerts
        vo.setActiveAlerts(alertMapper.countUnread());
        
        // Pending orders
        vo.setPendingInboundOrders(inboundOrderMapper.selectCount(
                new LambdaQueryWrapper<InboundOrder>().eq(InboundOrder::getOrderStatus, 1)).intValue());
        
        vo.setPendingOutboundOrders(outboundOrderMapper.selectCount(
                new LambdaQueryWrapper<OutboundOrder>().eq(OutboundOrder::getOrderStatus, 1)).intValue());
        
        return vo;
    }

    @Override
    public List<Map<String, Object>> getInventoryTrend(Long productId, Long warehouseId, String startDate, String endDate) {
        LambdaQueryWrapper<InventorySnapshot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(productId != null, InventorySnapshot::getProductId, productId)
               .eq(warehouseId != null, InventorySnapshot::getWarehouseId, warehouseId)
               .between(InventorySnapshot::getSnapshotDate, startDate, endDate)
               .orderByAsc(InventorySnapshot::getSnapshotDate);
        
        List<InventorySnapshot> snapshots = snapshotMapper.selectList(wrapper);
        
        // Group by date
        Map<Date, BigDecimal> dateMap = snapshots.stream()
                .collect(Collectors.groupingBy(
                        InventorySnapshot::getSnapshotDate,
                        Collectors.reducing(BigDecimal.ZERO, InventorySnapshot::getTotalQuantity, BigDecimal::add)));
        
        return dateMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("date", DateUtil.format(e.getKey(), "yyyy-MM-dd"));
                    m.put("quantity", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getInOutStats(Long warehouseId, String startDate, String endDate) {
        // Get inbound records
        LambdaQueryWrapper<InboundRecord> inWrapper = new LambdaQueryWrapper<>();
        inWrapper.eq(warehouseId != null, InboundRecord::getWarehouseId, warehouseId)
                 .between(InboundRecord::getOperateTime, startDate + " 00:00:00", endDate + " 23:59:59");
        List<InboundRecord> inRecords = inboundRecordMapper.selectList(inWrapper);
        
        // Get outbound records
        LambdaQueryWrapper<OutboundRecord> outWrapper = new LambdaQueryWrapper<>();
        outWrapper.eq(warehouseId != null, OutboundRecord::getWarehouseId, warehouseId)
                  .between(OutboundRecord::getOperateTime, startDate + " 00:00:00", endDate + " 23:59:59");
        List<OutboundRecord> outRecords = outboundRecordMapper.selectList(outWrapper);
        
        // Group by date
        Map<String, BigDecimal> inByDate = inRecords.stream()
                .collect(Collectors.groupingBy(
                        r -> DateUtil.format(r.getOperateTime(), "yyyy-MM-dd"),
                        Collectors.reducing(BigDecimal.ZERO, InboundRecord::getQuantity, BigDecimal::add)));
        
        Map<String, BigDecimal> outByDate = outRecords.stream()
                .collect(Collectors.groupingBy(
                        r -> DateUtil.format(r.getOperateTime(), "yyyy-MM-dd"),
                        Collectors.reducing(BigDecimal.ZERO, OutboundRecord::getQuantity, BigDecimal::add)));
        
        // Merge dates
        Set<String> allDates = new TreeSet<>();
        allDates.addAll(inByDate.keySet());
        allDates.addAll(outByDate.keySet());
        
        return allDates.stream().map(date -> {
            Map<String, Object> m = new HashMap<>();
            m.put("date", date);
            m.put("inQuantity", inByDate.getOrDefault(date, BigDecimal.ZERO));
            m.put("outQuantity", outByDate.getOrDefault(date, BigDecimal.ZERO));
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getProductRanking(String startDate, String endDate, Integer limit) {
        // Get outbound records in the period
        LambdaQueryWrapper<OutboundRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.between(OutboundRecord::getOperateTime, startDate + " 00:00:00", endDate + " 23:59:59");
        List<OutboundRecord> records = outboundRecordMapper.selectList(wrapper);
        
        // Group by product
        Map<Long, BigDecimal> productQty = records.stream()
                .collect(Collectors.groupingBy(
                        OutboundRecord::getProductId,
                        Collectors.reducing(BigDecimal.ZERO, OutboundRecord::getQuantity, BigDecimal::add)));
        
        // Sort and limit
        return productQty.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit != null ? limit : 10)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("productId", e.getKey());
                    Product product = productMapper.selectById(e.getKey());
                    m.put("productName", product != null ? product.getProductName() : "Unknown");
                    m.put("quantity", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }
}
