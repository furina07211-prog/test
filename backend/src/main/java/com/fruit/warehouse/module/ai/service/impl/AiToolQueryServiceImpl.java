package com.fruit.warehouse.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.warehouse.module.ai.dto.AiIntentResult;
import com.fruit.warehouse.module.ai.service.AiToolQueryService;
import com.fruit.warehouse.module.basic.entity.Fruit;
import com.fruit.warehouse.module.basic.entity.Warehouse;
import com.fruit.warehouse.module.basic.mapper.FruitMapper;
import com.fruit.warehouse.module.basic.mapper.WarehouseMapper;
import com.fruit.warehouse.module.dashboard.entity.AiPurchaseSuggestion;
import com.fruit.warehouse.module.dashboard.mapper.AiPurchaseSuggestionMapper;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import com.fruit.warehouse.module.inventory.mapper.InventoryBatchMapper;
import com.fruit.warehouse.module.sales.entity.SalesOrder;
import com.fruit.warehouse.module.sales.entity.SalesOrderItem;
import com.fruit.warehouse.module.sales.mapper.SalesOrderItemMapper;
import com.fruit.warehouse.module.sales.mapper.SalesOrderMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiToolQueryServiceImpl implements AiToolQueryService {

    private static final String INTENT_INVENTORY_BY_FRUIT = "INVENTORY_BY_FRUIT";
    private static final String INTENT_INVENTORY_OVERVIEW = "INVENTORY_OVERVIEW";
    private static final String INTENT_SALES_REPORT_PERIOD = "SALES_REPORT_PERIOD";
    private static final String INTENT_PURCHASE_SUGGESTION = "PURCHASE_SUGGESTION";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final InventoryBatchMapper inventoryBatchMapper;
    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final AiPurchaseSuggestionMapper aiPurchaseSuggestionMapper;
    private final FruitMapper fruitMapper;
    private final WarehouseMapper warehouseMapper;

    @Override
    public String execute(AiIntentResult intent) {
        if (intent == null || !StringUtils.hasText(intent.getIntentCode())) {
            return "未识别到可执行的业务意图，请明确说明“查库存”“销售报表”或“采购建议”。";
        }
        return switch (intent.getIntentCode()) {
            case INTENT_INVENTORY_BY_FRUIT -> inventoryByFruit(intent);
            case INTENT_INVENTORY_OVERVIEW -> inventoryOverview();
            case INTENT_SALES_REPORT_PERIOD -> salesReport(intent);
            case INTENT_PURCHASE_SUGGESTION -> purchaseSuggestion(intent);
            default -> "当前暂不支持该意图，请换一种说法再试。";
        };
    }

    private String inventoryByFruit(AiIntentResult intent) {
        if (intent.getFruitId() == null) {
            return inventoryOverview();
        }
        List<InventoryBatch> rows = inventoryBatchMapper.selectList(new LambdaQueryWrapper<InventoryBatch>()
                .eq(InventoryBatch::getFruitId, intent.getFruitId())
                .eq(InventoryBatch::getStatus, "IN_STOCK")
                .gt(InventoryBatch::getAvailableQty, BigDecimal.ZERO));

        BigDecimal total = rows.stream()
                .map(InventoryBatch::getAvailableQty)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        String fruitName = StringUtils.hasText(intent.getFruitName()) ? intent.getFruitName() : "该水果";
        if (rows.isEmpty()) {
            return fruitName + "当前可用库存为 0，未查询到在库批次。";
        }
        return String.format("%s 当前可用库存 %.2f，共 %d 个在库批次。", fruitName, total, rows.size());
    }

    private String inventoryOverview() {
        List<InventoryBatch> rows = inventoryBatchMapper.selectList(new LambdaQueryWrapper<InventoryBatch>()
                .eq(InventoryBatch::getStatus, "IN_STOCK")
                .gt(InventoryBatch::getAvailableQty, BigDecimal.ZERO));

        if (rows.isEmpty()) {
            return "当前系统在库可用库存为 0。";
        }

        Map<Long, BigDecimal> byFruit = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (InventoryBatch row : rows) {
            BigDecimal qty = nvl(row.getAvailableQty());
            total = total.add(qty);
            byFruit.merge(row.getFruitId(), qty, BigDecimal::add);
        }

        Map<Long, String> fruitNameMap = mapFruitNames(byFruit.keySet());
        List<Map.Entry<Long, BigDecimal>> top = byFruit.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(3)
                .toList();

        String detail = top.stream()
                .map(entry -> {
                    String name = fruitNameMap.getOrDefault(entry.getKey(), "水果-" + entry.getKey());
                    return name + " " + entry.getValue().setScale(2, RoundingMode.HALF_UP) + "kg";
                })
                .collect(Collectors.joining("；"));

        return "库存总览：当前可用库存 " + total.setScale(2, RoundingMode.HALF_UP)
                + "kg，覆盖 " + byFruit.size() + " 个水果品种。库存前3为：" + detail + "。";
    }

    private String salesReport(AiIntentResult intent) {
        LocalDateTime start = intent.getStartTime() == null ? LocalDate.now().minusDays(1).atStartOfDay() : intent.getStartTime();
        LocalDateTime end = intent.getEndTime() == null ? LocalDate.now().atStartOfDay() : intent.getEndTime();

        List<SalesOrder> orders = salesOrderMapper.selectList(new LambdaQueryWrapper<SalesOrder>()
                .in(SalesOrder::getOrderStatus, List.of("SHIPPED", "CONFIRMED"))
                .ge(SalesOrder::getOrderTime, start)
                .lt(SalesOrder::getOrderTime, end));

        if (orders.isEmpty()) {
            return "销售报表（" + DATE_FMT.format(start) + " ~ " + DATE_FMT.format(end) + "）：无成交订单。";
        }

        BigDecimal totalAmount = orders.stream()
                .map(SalesOrder::getTotalAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal avg = totalAmount.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
        Set<Long> orderIds = orders.stream().map(SalesOrder::getId).collect(Collectors.toSet());
        List<SalesOrderItem> items = salesOrderItemMapper.selectList(new LambdaQueryWrapper<SalesOrderItem>()
                .in(SalesOrderItem::getSalesOrderId, orderIds));

        Map<Long, BigDecimal> qtyByFruit = new HashMap<>();
        for (SalesOrderItem item : items) {
            qtyByFruit.merge(item.getFruitId(), nvl(item.getQuantity()), BigDecimal::add);
        }

        String topFruit = "暂无";
        if (!qtyByFruit.isEmpty()) {
            Map.Entry<Long, BigDecimal> best = qtyByFruit.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            if (best != null) {
                String fruitName = mapFruitNames(Set.of(best.getKey())).getOrDefault(best.getKey(), "水果-" + best.getKey());
                topFruit = fruitName + "（" + best.getValue().setScale(2, RoundingMode.HALF_UP) + "kg）";
            }
        }

        return "销售报表（" + DATE_FMT.format(start) + " ~ " + DATE_FMT.format(end) + "）：订单 " + orders.size()
                + " 笔，销售额 " + totalAmount + "，客单价 " + avg + "，销量TOP水果为 " + topFruit + "。";
    }

    private String purchaseSuggestion(AiIntentResult intent) {
        LambdaQueryWrapper<AiPurchaseSuggestion> wrapper = new LambdaQueryWrapper<AiPurchaseSuggestion>()
                .eq(intent.getFruitId() != null, AiPurchaseSuggestion::getFruitId, intent.getFruitId())
                .orderByDesc(AiPurchaseSuggestion::getSuggestionDate)
                .orderByDesc(AiPurchaseSuggestion::getCreatedTime)
                .last("limit 5");

        List<AiPurchaseSuggestion> list = aiPurchaseSuggestionMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return "当前暂无可用采购建议数据。建议先在看板执行预测与库存优化后再查询。";
        }

        Map<Long, String> fruitNameMap = mapFruitNames(list.stream().map(AiPurchaseSuggestion::getFruitId).collect(Collectors.toSet()));
        Map<Long, String> warehouseNameMap = mapWarehouseNames(list.stream().map(AiPurchaseSuggestion::getWarehouseId).collect(Collectors.toSet()));

        List<String> lines = new ArrayList<>();
        for (AiPurchaseSuggestion row : list.stream().limit(3).toList()) {
            String fruitName = fruitNameMap.getOrDefault(row.getFruitId(), "水果-" + row.getFruitId());
            String warehouseName = warehouseNameMap.getOrDefault(row.getWarehouseId(), "仓库-" + row.getWarehouseId());
            lines.add(fruitName + "@" + warehouseName + " 建议采购 "
                    + nvl(row.getRecommendedPurchaseQty()).setScale(2, RoundingMode.HALF_UP) + "kg");
        }

        return "采购建议：" + String.join("；", lines) + "。";
    }

    private Map<Long, String> mapFruitNames(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return fruitMapper.selectBatchIds(ids).stream()
                .filter(item -> item != null)
                .collect(Collectors.toMap(Fruit::getId, Fruit::getFruitName, (a, b) -> a, LinkedHashMap::new));
    }

    private Map<Long, String> mapWarehouseNames(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return warehouseMapper.selectBatchIds(ids).stream()
                .filter(item -> item != null)
                .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName, (a, b) -> a, LinkedHashMap::new));
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}