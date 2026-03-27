package com.fruit.warehouse.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.warehouse.module.ai.dto.AiIntentResult;
import com.fruit.warehouse.module.ai.service.AiToolQueryService;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import com.fruit.warehouse.module.inventory.mapper.InventoryBatchMapper;
import com.fruit.warehouse.module.sales.entity.SalesOrder;
import com.fruit.warehouse.module.sales.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiToolQueryServiceImpl implements AiToolQueryService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final InventoryBatchMapper inventoryBatchMapper;
    private final SalesOrderMapper salesOrderMapper;

    @Override
    public String execute(AiIntentResult intent) {
        if (intent == null || intent.getIntentCode() == null) {
            return "未识别到可执行的业务查询意图。";
        }
        return switch (intent.getIntentCode()) {
            case "INVENTORY_BY_FRUIT" -> inventoryByFruit(intent);
            case "SALES_REPORT_YESTERDAY" -> salesYesterday(intent);
            default -> "暂不支持该意图，请换个问法。";
        };
    }

    private String inventoryByFruit(AiIntentResult intent) {
        List<InventoryBatch> rows = inventoryBatchMapper.selectList(new LambdaQueryWrapper<InventoryBatch>()
            .eq(InventoryBatch::getFruitId, intent.getFruitId())
            .gt(InventoryBatch::getAvailableQty, BigDecimal.ZERO));

        BigDecimal total = rows.stream()
            .map(InventoryBatch::getAvailableQty)
            .filter(v -> v != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (rows.isEmpty()) {
            return "当前未查询到" + intent.getFruitName() + "在库批次，可用库存为 0。";
        }

        return String.format("%s 当前可用库存 %.2f，共 %d 个在库批次。", intent.getFruitName(), total, rows.size());
    }

    private String salesYesterday(AiIntentResult intent) {
        List<SalesOrder> rows = salesOrderMapper.selectList(new LambdaQueryWrapper<SalesOrder>()
            .ge(SalesOrder::getOrderTime, intent.getStartTime())
            .lt(SalesOrder::getOrderTime, intent.getEndTime()));

        BigDecimal totalAmount = rows.stream()
            .map(SalesOrder::getTotalAmount)
            .filter(v -> v != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        String period = intent.getStartTime().format(DATE_TIME_FORMATTER) + " ~ " + intent.getEndTime().format(DATE_TIME_FORMATTER);
        return String.format("昨日销售统计（%s）：订单 %d 笔，销售额 %.2f。", period, rows.size(), totalAmount);
    }
}
