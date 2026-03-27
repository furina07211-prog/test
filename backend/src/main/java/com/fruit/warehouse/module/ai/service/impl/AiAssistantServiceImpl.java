package com.fruit.warehouse.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.ai.dto.AiAssistantConfirmRequest;
import com.fruit.warehouse.module.ai.dto.AiAssistantConfirmResponse;
import com.fruit.warehouse.module.ai.dto.AiAssistantDispatchRequest;
import com.fruit.warehouse.module.ai.dto.AiAssistantDispatchResponse;
import com.fruit.warehouse.module.ai.dto.AiAssistantHistoryItem;
import com.fruit.warehouse.module.ai.entity.AiChatLog;
import com.fruit.warehouse.module.ai.service.AiAssistantService;
import com.fruit.warehouse.module.ai.service.AiChatLogService;
import com.fruit.warehouse.module.basic.entity.Customer;
import com.fruit.warehouse.module.basic.entity.Fruit;
import com.fruit.warehouse.module.basic.entity.Supplier;
import com.fruit.warehouse.module.basic.entity.Warehouse;
import com.fruit.warehouse.module.basic.mapper.CustomerMapper;
import com.fruit.warehouse.module.basic.mapper.FruitMapper;
import com.fruit.warehouse.module.basic.mapper.SupplierMapper;
import com.fruit.warehouse.module.basic.mapper.WarehouseMapper;
import com.fruit.warehouse.module.dashboard.dto.SalesTopPoint;
import com.fruit.warehouse.module.dashboard.dto.WarningItemPoint;
import com.fruit.warehouse.module.dashboard.mapper.DashboardQueryMapper;
import com.fruit.warehouse.module.inventory.entity.InventoryBatch;
import com.fruit.warehouse.module.inventory.mapper.InventoryBatchMapper;
import com.fruit.warehouse.module.purchase.dto.PurchaseOrderCreateRequest;
import com.fruit.warehouse.module.purchase.entity.PurchaseOrder;
import com.fruit.warehouse.module.purchase.service.PurchaseService;
import com.fruit.warehouse.module.sales.dto.SalesOrderCreateRequest;
import com.fruit.warehouse.module.sales.entity.SalesOrder;
import com.fruit.warehouse.module.sales.mapper.SalesOrderMapper;
import com.fruit.warehouse.module.sales.service.SalesService;
import io.jsonwebtoken.Claims;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI智能助手 模块服务实现。
 */
@Service
@RequiredArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private static final String INTENT_CREATE_PURCHASE_DRAFT = "CREATE_PURCHASE_DRAFT";
    private static final String INTENT_CREATE_SALES_DRAFT = "CREATE_SALES_DRAFT";
    private static final String INTENT_QUERY_ALERTS = "QUERY_ALERTS";
    private static final String INTENT_QUERY_NEAR_EXPIRY = "QUERY_NEAR_EXPIRY";
    private static final String INTENT_QUERY_SALES_RANK = "QUERY_SALES_RANK";
    private static final String INTENT_REPORT_SALES_SUMMARY = "REPORT_SALES_SUMMARY";

    private static final String TOOL_ASSISTANT = "assistant-router";
    private static final String TOOL_CONFIRM = "assistant-confirm";
    private static final String PROVIDER_ASSISTANT = "assistant";
    private static final String MODEL_ASSISTANT = "rule-router-v2";
    private static final String MODEL_CONFIRM = "rule-confirm-v1";
    private static final String BIZ_PURCHASE_ORDER = "PURCHASE_ORDER";
    private static final String BIZ_SALES_ORDER = "SALES_ORDER";
    private static final BigDecimal HALF = new BigDecimal("0.5");

    private static final Pattern QTY_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(斤|公斤|kg|千克)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern PRICE_PATTERN = Pattern.compile("(?:单价|每斤|每公斤|每kg|价格)\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern DAY_PATTERN = Pattern.compile("(\\d+)\\s*天");
    private static final Pattern TOP_PATTERN = Pattern.compile("(?:top|前)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private final PendingActionStore pendingActionStore;
    private final AiChatLogService aiChatLogService;
    private final PurchaseService purchaseService;
    private final SalesService salesService;
    private final SupplierMapper supplierMapper;
    private final CustomerMapper customerMapper;
    private final WarehouseMapper warehouseMapper;
    private final FruitMapper fruitMapper;
    private final InventoryBatchMapper inventoryBatchMapper;
    private final SalesOrderMapper salesOrderMapper;
    private final DashboardQueryMapper dashboardQueryMapper;

    /**
     * 智能助手分发：识别意图并返回查询结果或待确认预览。
     */
    @Override
    public AiAssistantDispatchResponse dispatch(AiAssistantDispatchRequest request) {
        String message = request == null ? null : request.getMessage();
        if (!StringUtils.hasText(message)) {
            throw new BusinessException("消息内容不能为空");
        }

        Long userId = currentUserId();
        String sessionId = normalizeSessionId(request.getSessionId(), userId);
        aiChatLogService.log(userId, sessionId, "user", message, null, TOOL_ASSISTANT, PROVIDER_ASSISTANT, MODEL_ASSISTANT, false);

        IntentDecision decision = detectIntent(message);
        AiAssistantDispatchResponse response;
        if (!decision.isMatched()) {
            response = AiAssistantDispatchResponse.builder()
                .matched(false)
                .answer("未命中业务指令，我将切换到通用对话模式继续回答。")
                .requiresConfirm(false)
                .build();
        } else {
            response = switch (decision.getIntentCode()) {
                case INTENT_CREATE_PURCHASE_DRAFT -> buildDraftPreview(message, sessionId, userId, true);
                case INTENT_CREATE_SALES_DRAFT -> buildDraftPreview(message, sessionId, userId, false);
                case INTENT_QUERY_ALERTS -> buildAlertSummary(message);
                case INTENT_QUERY_NEAR_EXPIRY -> buildNearExpirySummary(message);
                case INTENT_QUERY_SALES_RANK -> buildSalesRankSummary(message);
                case INTENT_REPORT_SALES_SUMMARY -> buildSalesReportSummary(message);
                default -> AiAssistantDispatchResponse.builder()
                    .matched(false)
                    .answer("未识别到可执行业务意图，请换个说法再试。")
                    .requiresConfirm(false)
                    .build();
            };
        }

        aiChatLogService.log(
            userId,
            sessionId,
            "assistant",
            response.getAnswer(),
            response.getIntentCode(),
            TOOL_ASSISTANT,
            PROVIDER_ASSISTANT,
            MODEL_ASSISTANT,
            false
        );
        return response;
    }

    /**
     * 智能助手确认：对待执行动作进行确认/取消。
     */
    @Override
    public AiAssistantConfirmResponse confirm(AiAssistantConfirmRequest request) {
        if (request == null || !StringUtils.hasText(request.getActionId())) {
            throw new BusinessException("actionId 不能为空");
        }
        Long userId = currentUserId();
        String sessionId = StringUtils.hasText(request.getSessionId()) ? request.getSessionId() : null;
        aiChatLogService.log(
            userId,
            sessionId,
            "user",
            Boolean.FALSE.equals(request.getConfirm()) ? "取消执行 " + request.getActionId() : "确认执行 " + request.getActionId(),
            null,
            TOOL_CONFIRM,
            PROVIDER_ASSISTANT,
            MODEL_CONFIRM,
            false
        );

        PendingActionStore.PendingAction action = pendingActionStore.get(request.getActionId());
        if (action == null) {
            AiAssistantConfirmResponse response = AiAssistantConfirmResponse.builder()
                .success(false)
                .answer("确认超时或动作不存在，请重新发起。")
                .build();
            aiChatLogService.log(userId, sessionId, "assistant", response.getAnswer(), null, TOOL_CONFIRM, PROVIDER_ASSISTANT, MODEL_CONFIRM, false);
            return response;
        }
        if (userId != null && action.getUserId() != null && !Objects.equals(userId, action.getUserId())) {
            AiAssistantConfirmResponse response = AiAssistantConfirmResponse.builder()
                .success(false)
                .answer("该确认动作不属于当前用户，无法执行。")
                .build();
            aiChatLogService.log(userId, sessionId, "assistant", response.getAnswer(), null, TOOL_CONFIRM, PROVIDER_ASSISTANT, MODEL_CONFIRM, false);
            return response;
        }
        if (StringUtils.hasText(sessionId) && StringUtils.hasText(action.getSessionId()) && !Objects.equals(sessionId, action.getSessionId())) {
            AiAssistantConfirmResponse response = AiAssistantConfirmResponse.builder()
                .success(false)
                .answer("会话不匹配，请回到原会话重新确认。")
                .build();
            aiChatLogService.log(userId, sessionId, "assistant", response.getAnswer(), null, TOOL_CONFIRM, PROVIDER_ASSISTANT, MODEL_CONFIRM, false);
            return response;
        }

        if (Boolean.FALSE.equals(request.getConfirm())) {
            pendingActionStore.remove(request.getActionId());
            AiAssistantConfirmResponse response = AiAssistantConfirmResponse.builder()
                .success(true)
                .answer("已取消本次操作，不会写入业务数据。")
                .build();
            aiChatLogService.log(userId, sessionId, "assistant", response.getAnswer(), action.getIntentCode(), TOOL_CONFIRM, PROVIDER_ASSISTANT, MODEL_CONFIRM, false);
            return response;
        }

        PendingActionStore.PendingAction consumed = pendingActionStore.consume(request.getActionId());
        if (consumed == null) {
            AiAssistantConfirmResponse response = AiAssistantConfirmResponse.builder()
                .success(false)
                .answer("确认动作已失效，请重新发起。")
                .build();
            aiChatLogService.log(userId, sessionId, "assistant", response.getAnswer(), null, TOOL_CONFIRM, PROVIDER_ASSISTANT, MODEL_CONFIRM, false);
            return response;
        }

        AiAssistantConfirmResponse response = executeAction(consumed);
        aiChatLogService.log(
            userId,
            consumed.getSessionId(),
            "assistant",
            response.getAnswer(),
            consumed.getIntentCode(),
            TOOL_CONFIRM,
            PROVIDER_ASSISTANT,
            MODEL_CONFIRM,
            false
        );
        return response;
    }

    /**
     * 查询会话历史并转换为前端展示结构。
     */
    @Override
    public IPage<AiAssistantHistoryItem> history(String sessionId, Integer pageNo, Integer pageSize) {
        if (!StringUtils.hasText(sessionId)) {
            throw new BusinessException("sessionId 不能为空");
        }
        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 20 : pageSize;
        Long userId = currentUserId();
        IPage<AiChatLog> page = aiChatLogService.pageBySession(userId, sessionId, current, size);

        List<AiAssistantHistoryItem> records = page.getRecords().stream().map(log -> {
            AiAssistantHistoryItem item = new AiAssistantHistoryItem();
            item.setRole("user".equalsIgnoreCase(log.getMessageType()) ? "user" : "assistant");
            item.setContent(log.getContent());
            item.setCreateTime(log.getCreateTime());
            item.setIntentCode(log.getIntentCode());
            item.setToolName(log.getToolName());
            return item;
        }).toList();

        Page<AiAssistantHistoryItem> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    private AiAssistantDispatchResponse buildDraftPreview(String message, String sessionId, Long userId, boolean purchase) {
        List<String> clarifications = new ArrayList<>();

        ResolveResult<Fruit> fruitResult = resolveEntity(
            message,
            fruitMapper.selectList(new LambdaQueryWrapper<Fruit>().eq(Fruit::getStatus, 1)),
            Fruit::getFruitName,
            "水果"
        );
        if (StringUtils.hasText(fruitResult.getClarification())) {
            clarifications.add(fruitResult.getClarification());
        }

        ResolveResult<Warehouse> warehouseResult = resolveEntity(
            message,
            warehouseMapper.selectList(new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getStatus, 1)),
            Warehouse::getWarehouseName,
            "仓库"
        );
        if (!warehouseResult.isResolved()) {
            List<Warehouse> allWarehouses = warehouseMapper.selectList(new LambdaQueryWrapper<Warehouse>().eq(Warehouse::getStatus, 1));
            if (allWarehouses.size() == 1) {
                warehouseResult = ResolveResult.resolved(allWarehouses.get(0));
            } else if (StringUtils.hasText(warehouseResult.getClarification())) {
                clarifications.add(warehouseResult.getClarification());
            } else {
                clarifications.add("请补充仓库名称，例如“华南仓”。");
            }
        }

        ResolveResult<Supplier> supplierResult = null;
        ResolveResult<Customer> customerResult = null;
        if (purchase) {
            supplierResult = resolveEntity(
                message,
                supplierMapper.selectList(new LambdaQueryWrapper<Supplier>().eq(Supplier::getStatus, 1)),
                Supplier::getSupplierName,
                "供应商"
            );
            if (StringUtils.hasText(supplierResult.getClarification())) {
                clarifications.add(supplierResult.getClarification());
            }
        } else {
            customerResult = resolveEntity(
                message,
                customerMapper.selectList(new LambdaQueryWrapper<Customer>().eq(Customer::getStatus, 1)),
                Customer::getCustomerName,
                "客户"
            );
            if (StringUtils.hasText(customerResult.getClarification())) {
                clarifications.add(customerResult.getClarification());
            }
        }

        QuantityParseResult qtyResult = parseQuantity(message);
        if (qtyResult == null || qtyResult.getQtyKg() == null || qtyResult.getQtyKg().compareTo(BigDecimal.ZERO) <= 0) {
            clarifications.add("请补充有效数量，例如“100斤”或“50kg”。");
        }

        Fruit fruit = fruitResult.getEntity();
        if (fruit == null) {
            clarifications.add("请补充水果名称，例如“苹果”。");
        } else if (!"kg".equalsIgnoreCase(fruit.getUnit())) {
            clarifications.add("当前水果单位不是kg，系统无法自动换算，请先确认水果单位后再创建。");
        }

        if (purchase && (supplierResult == null || supplierResult.getEntity() == null)) {
            clarifications.add("请补充供应商名称。");
        }
        if (!purchase && (customerResult == null || customerResult.getEntity() == null)) {
            clarifications.add("请补充客户名称。");
        }

        if (!clarifications.isEmpty()) {
            return AiAssistantDispatchResponse.builder()
                .matched(true)
                .intentCode(purchase ? INTENT_CREATE_PURCHASE_DRAFT : INTENT_CREATE_SALES_DRAFT)
                .requiresConfirm(false)
                .clarifications(clarifications)
                .answer("已识别到建单意图，但信息还不完整，请按提示补充后重试。")
                .build();
        }

        BigDecimal unitPrice = parseUnitPrice(message);
        if (unitPrice == null) {
            unitPrice = purchase ? nvl(fruit.getSuggestedPurchasePrice()) : nvl(fruit.getSuggestedSalePrice());
        }

        BigDecimal qtyKg = qtyResult.getQtyKg().setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = qtyKg.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> preview = new LinkedHashMap<>();
        preview.put("bizType", purchase ? "采购单" : "销售单");
        preview.put("warehouseName", warehouseResult.getEntity().getWarehouseName());
        preview.put("fruitName", fruit.getFruitName());
        preview.put("quantityKg", qtyKg);
        preview.put("sourceQtyText", qtyResult.getSourceText());
        preview.put("unitPrice", unitPrice);
        preview.put("estimatedAmount", amount);
        preview.put("remark", "AI助手创建草稿");
        if (purchase) {
            preview.put("partnerName", supplierResult.getEntity().getSupplierName());
        } else {
            preview.put("partnerName", customerResult.getEntity().getCustomerName());
        }
        List<Map<String, Object>> itemPreviews = new ArrayList<>();
        itemPreviews.add(Map.of(
            "fruitId", fruit.getId(),
            "fruitName", fruit.getFruitName(),
            "quantityKg", qtyKg,
            "unitPrice", unitPrice,
            "amount", amount
        ));
        preview.put("items", itemPreviews);

        Map<String, Object> payload = new HashMap<>();
        payload.put("warehouseId", warehouseResult.getEntity().getId());
        payload.put("fruitId", fruit.getId());
        payload.put("quantityKg", qtyKg);
        payload.put("unitPrice", unitPrice);
        payload.put("sessionId", sessionId);
        payload.put("createTime", LocalDateTime.now().toString());
        if (purchase) {
            payload.put("supplierId", supplierResult.getEntity().getId());
        } else {
            payload.put("customerId", customerResult.getEntity().getId());
        }

        String intentCode = purchase ? INTENT_CREATE_PURCHASE_DRAFT : INTENT_CREATE_SALES_DRAFT;
        String actionId = pendingActionStore.put(userId, sessionId, intentCode, payload);

        return AiAssistantDispatchResponse.builder()
            .matched(true)
            .intentCode(intentCode)
            .requiresConfirm(true)
            .actionId(actionId)
            .preview(preview)
            .answer("已生成草稿预览，请点击“确认创建”后写入系统。")
            .build();
    }

    private AiAssistantDispatchResponse buildAlertSummary(String message) {
        int limit = resolvePositiveInt(message, TOP_PATTERN, 20, 1, 50);
        List<WarningItemPoint> warningItems = dashboardQueryMapper.selectWarningItems(limit);
        if (warningItems == null || warningItems.isEmpty()) {
            return AiAssistantDispatchResponse.builder()
                .matched(true)
                .intentCode(INTENT_QUERY_ALERTS)
                .requiresConfirm(false)
                .answer("当前没有未处理的库存预警。")
                .build();
        }

        Map<String, Long> levelCount = warningItems.stream()
            .collect(Collectors.groupingBy(item -> safeText(item.getAlertLevel(), "UNKNOWN"), LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> fruitCount = warningItems.stream()
            .collect(Collectors.groupingBy(item -> safeText(item.getFruitName(), "未知水果"), Collectors.counting()));
        List<Map.Entry<String, Long>> topFruits = fruitCount.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(3)
            .toList();

        StringBuilder answer = new StringBuilder();
        answer.append("当前未处理预警共 ").append(warningItems.size()).append(" 条。\n");
        answer.append("等级分布：")
            .append(levelCount.entrySet().stream().map(e -> e.getKey() + " " + e.getValue() + "条").collect(Collectors.joining("，")))
            .append("。\n");
        if (!topFruits.isEmpty()) {
            answer.append("高频预警商品：")
                .append(topFruits.stream().map(e -> e.getKey() + "(" + e.getValue() + "条)").collect(Collectors.joining("，")))
                .append("。\n");
        }
        WarningItemPoint first = warningItems.get(0);
        answer.append("最近一条：")
            .append(safeText(first.getFruitName(), "未知水果")).append(" / ")
            .append(safeText(first.getWarehouseName(), "未知仓库")).append(" / ")
            .append(safeText(first.getAlertType(), "UNKNOWN")).append(" / ")
            .append(safeText(first.getAlertLevel(), "UNKNOWN"));

        return AiAssistantDispatchResponse.builder()
            .matched(true)
            .intentCode(INTENT_QUERY_ALERTS)
            .requiresConfirm(false)
            .answer(answer.toString())
            .build();
    }

    private AiAssistantDispatchResponse buildNearExpirySummary(String message) {
        int days = resolvePositiveInt(message, DAY_PATTERN, 3, 1, 30);
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);

        List<InventoryBatch> batches = inventoryBatchMapper.selectList(
            new LambdaQueryWrapper<InventoryBatch>()
                .eq(InventoryBatch::getStatus, "IN_STOCK")
                .gt(InventoryBatch::getAvailableQty, BigDecimal.ZERO)
                .isNotNull(InventoryBatch::getExpirationDate)
                .ge(InventoryBatch::getExpirationDate, today)
                .le(InventoryBatch::getExpirationDate, endDate)
                .orderByAsc(InventoryBatch::getExpirationDate)
                .last("LIMIT 20")
        );
        if (batches.isEmpty()) {
            return AiAssistantDispatchResponse.builder()
                .matched(true)
                .intentCode(INTENT_QUERY_NEAR_EXPIRY)
                .requiresConfirm(false)
                .answer("未来" + days + "天内暂无临期商品。")
                .build();
        }

        Map<Long, String> fruitNameMap = mapFruitName(batches.stream().map(InventoryBatch::getFruitId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, String> warehouseNameMap = mapWarehouseName(batches.stream().map(InventoryBatch::getWarehouseId).filter(Objects::nonNull).collect(Collectors.toSet()));

        StringBuilder answer = new StringBuilder();
        answer.append("未来").append(days).append("天内共有 ").append(batches.size()).append(" 个临期批次：\n");
        int idx = 1;
        for (InventoryBatch batch : batches) {
            long leftDays = ChronoUnit.DAYS.between(today, batch.getExpirationDate());
            answer.append(idx++).append(". ")
                .append(safeText(fruitNameMap.get(batch.getFruitId()), "未知水果"))
                .append(" / ").append(safeText(warehouseNameMap.get(batch.getWarehouseId()), "未知仓库"))
                .append(" / 批次 ").append(safeText(batch.getBatchNo(), "-"))
                .append(" / 可用 ").append(nvl(batch.getAvailableQty()).setScale(2, RoundingMode.HALF_UP)).append("kg")
                .append(" / 剩余 ").append(leftDays).append(" 天\n");
        }

        return AiAssistantDispatchResponse.builder()
            .matched(true)
            .intentCode(INTENT_QUERY_NEAR_EXPIRY)
            .requiresConfirm(false)
            .answer(answer.toString().trim())
            .build();
    }
    private AiAssistantDispatchResponse buildSalesRankSummary(String message) {
        int days = resolvePositiveInt(message, DAY_PATTERN, 30, 1, 365);
        int limit = resolvePositiveInt(message, TOP_PATTERN, 5, 1, 20);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        List<SalesTopPoint> points = dashboardQueryMapper.selectSalesTop(startDate, endDate, limit);
        if (points == null || points.isEmpty()) {
            return AiAssistantDispatchResponse.builder()
                .matched(true)
                .intentCode(INTENT_QUERY_SALES_RANK)
                .requiresConfirm(false)
                .answer("近" + days + "天暂无已完成销售数据。")
                .build();
        }

        StringBuilder answer = new StringBuilder();
        answer.append("近").append(days).append("天销量TOP").append(limit).append("：\n");
        for (int i = 0; i < points.size(); i++) {
            SalesTopPoint p = points.get(i);
            answer.append(i + 1).append(". ")
                .append(safeText(p.getFruitName(), "未知水果"))
                .append("：销量 ").append(nvl(p.getSalesQty()).setScale(2, RoundingMode.HALF_UP)).append("kg")
                .append("，销售额 ").append(nvl(p.getSalesAmount()).setScale(2, RoundingMode.HALF_UP))
                .append("\n");
        }

        return AiAssistantDispatchResponse.builder()
            .matched(true)
            .intentCode(INTENT_QUERY_SALES_RANK)
            .requiresConfirm(false)
            .answer(answer.toString().trim())
            .build();
    }

    private AiAssistantDispatchResponse buildSalesReportSummary(String message) {
        ReportRange range = resolveReportRange(message);
        LocalDateTime startTime = range.getStartDate().atStartOfDay();
        LocalDateTime endTime = range.getEndDate().plusDays(1).atStartOfDay();
        List<SalesOrder> currentOrders = listCompletedSales(startTime, endTime);

        BigDecimal amount = currentOrders.stream()
            .map(SalesOrder::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        long orderCount = currentOrders.size();
        BigDecimal avg = orderCount == 0 ? BigDecimal.ZERO : amount.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);

        List<SalesTopPoint> topOne = dashboardQueryMapper.selectSalesTop(range.getStartDate(), range.getEndDate(), 1);
        SalesTopPoint best = topOne.isEmpty() ? null : topOne.get(0);

        long periodDays = ChronoUnit.DAYS.between(range.getStartDate(), range.getEndDate()) + 1;
        LocalDate prevEnd = range.getStartDate().minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(periodDays - 1);
        BigDecimal prevAmount = listCompletedSales(prevStart.atStartOfDay(), prevEnd.plusDays(1).atStartOfDay()).stream()
            .map(SalesOrder::getTotalAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        String conclusion = compareConclusion(amount, prevAmount);

        StringBuilder answer = new StringBuilder();
        answer.append("销售报表（").append(range.getLabel()).append("）\n")
            .append("订单数：").append(orderCount).append(" 笔\n")
            .append("销售额：").append(amount.setScale(2, RoundingMode.HALF_UP)).append("\n")
            .append("客单价：").append(avg).append("\n")
            .append("TOP水果：").append(best == null ? "暂无" : safeText(best.getFruitName(), "未知水果") + "（" + nvl(best.getSalesQty()).setScale(2, RoundingMode.HALF_UP) + "kg）").append("\n")
            .append("环比结论：").append(conclusion);

        return AiAssistantDispatchResponse.builder()
            .matched(true)
            .intentCode(INTENT_REPORT_SALES_SUMMARY)
            .requiresConfirm(false)
            .answer(answer.toString())
            .build();
    }

    private AiAssistantConfirmResponse executeAction(PendingActionStore.PendingAction action) {
        if (INTENT_CREATE_PURCHASE_DRAFT.equals(action.getIntentCode())) {
            PurchaseOrder order = createPurchaseDraft(action.getPayload());
            return AiAssistantConfirmResponse.builder()
                .success(true)
                .answer("采购草稿单已创建，单号：" + safeText(order.getPurchaseNo(), String.valueOf(order.getId())))
                .bizType(BIZ_PURCHASE_ORDER)
                .bizId(order.getId())
                .build();
        }
        if (INTENT_CREATE_SALES_DRAFT.equals(action.getIntentCode())) {
            SalesOrder order = createSalesDraft(action.getPayload());
            return AiAssistantConfirmResponse.builder()
                .success(true)
                .answer("销售草稿单已创建，单号：" + safeText(order.getSalesNo(), String.valueOf(order.getId())))
                .bizType(BIZ_SALES_ORDER)
                .bizId(order.getId())
                .build();
        }
        throw new BusinessException("不支持的确认动作：" + action.getIntentCode());
    }

    private PurchaseOrder createPurchaseDraft(Map<String, Object> payload) {
        Long supplierId = castLong(payload.get("supplierId"));
        Long warehouseId = castLong(payload.get("warehouseId"));
        Long fruitId = castLong(payload.get("fruitId"));
        BigDecimal qty = castBigDecimal(payload.get("quantityKg"));
        BigDecimal unitPrice = castBigDecimal(payload.get("unitPrice"));
        if (supplierId == null || warehouseId == null || fruitId == null || qty == null || unitPrice == null) {
            throw new BusinessException("采购预览参数缺失，请重新发起。");
        }

        Fruit fruit = fruitMapper.selectById(fruitId);
        if (fruit == null || fruit.getStatus() == null || fruit.getStatus() != 1) {
            throw new BusinessException("水果不存在或已停用，请重新发起。");
        }

        PurchaseOrderCreateRequest req = new PurchaseOrderCreateRequest();
        req.setSupplierId(supplierId);
        req.setWarehouseId(warehouseId);
        req.setOrderDate(LocalDate.now());
        req.setExpectedArrivalDate(LocalDate.now().plusDays(1));
        req.setRemark("AI助手创建草稿（待人工复核）");

        PurchaseOrderCreateRequest.PurchaseItemRequest item = new PurchaseOrderCreateRequest.PurchaseItemRequest();
        item.setFruitId(fruitId);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setBatchNo("AI" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        item.setProductionDate(LocalDate.now());
        int shelfLife = fruit.getShelfLifeDays() == null || fruit.getShelfLifeDays() <= 0 ? 7 : fruit.getShelfLifeDays();
        item.setExpirationDate(LocalDate.now().plusDays(shelfLife));
        item.setRemark("AI助手预填");
        req.setItems(Collections.singletonList(item));
        return purchaseService.createOrder(req);
    }

    private SalesOrder createSalesDraft(Map<String, Object> payload) {
        Long customerId = castLong(payload.get("customerId"));
        Long warehouseId = castLong(payload.get("warehouseId"));
        Long fruitId = castLong(payload.get("fruitId"));
        BigDecimal qty = castBigDecimal(payload.get("quantityKg"));
        BigDecimal unitPrice = castBigDecimal(payload.get("unitPrice"));
        if (customerId == null || warehouseId == null || fruitId == null || qty == null || unitPrice == null) {
            throw new BusinessException("销售预览参数缺失，请重新发起。");
        }

        Fruit fruit = fruitMapper.selectById(fruitId);
        if (fruit == null || fruit.getStatus() == null || fruit.getStatus() != 1) {
            throw new BusinessException("水果不存在或已停用，请重新发起。");
        }

        SalesOrderCreateRequest req = new SalesOrderCreateRequest();
        req.setCustomerId(customerId);
        req.setWarehouseId(warehouseId);
        req.setOrderTime(LocalDateTime.now());
        req.setRemark("AI助手创建草稿（待人工复核）");

        SalesOrderCreateRequest.SalesItemRequest item = new SalesOrderCreateRequest.SalesItemRequest();
        item.setFruitId(fruitId);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setRemark("AI助手预填");
        req.setItems(Collections.singletonList(item));
        return salesService.createOrder(req);
    }
    private IntentDecision detectIntent(String message) {
        String text = normalizeText(message);
        if (!StringUtils.hasText(text)) {
            return IntentDecision.unmatched();
        }

        int purchaseScore = 0;
        purchaseScore += hitScore(text, "采购", "进货", "补货", "采购单", "向");
        purchaseScore += hitScore(text, "供应商", "到货", "收货");
        if (containsAny(text, "销售", "销量", "报表")) {
            purchaseScore -= 2;
        }

        int salesScore = 0;
        salesScore += hitScore(text, "销售", "出货", "卖", "销售单");
        salesScore += hitScore(text, "客户", "发货", "出库");
        if (containsAny(text, "采购", "供应商")) {
            salesScore -= 2;
        }

        int alertScore = hitScore(text, "预警", "告警", "风险", "报警");
        int nearExpiryScore = hitScore(text, "临期", "过期", "即将过期", "保质期", "到期");
        int salesRankScore = hitScore(text, "销量排行", "销量排名", "热销", "top", "排名", "排行");
        int salesReportScore = hitScore(text, "报表", "分析", "总结", "客单价", "销售额");

        Map<String, Integer> scoreMap = new LinkedHashMap<>();
        scoreMap.put(INTENT_CREATE_PURCHASE_DRAFT, purchaseScore);
        scoreMap.put(INTENT_CREATE_SALES_DRAFT, salesScore);
        scoreMap.put(INTENT_QUERY_ALERTS, alertScore);
        scoreMap.put(INTENT_QUERY_NEAR_EXPIRY, nearExpiryScore);
        scoreMap.put(INTENT_QUERY_SALES_RANK, salesRankScore);
        scoreMap.put(INTENT_REPORT_SALES_SUMMARY, salesReportScore);

        Map.Entry<String, Integer> top = scoreMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);
        if (top == null || top.getValue() < 2) {
            return IntentDecision.unmatched();
        }
        long tied = scoreMap.values().stream().filter(v -> Objects.equals(v, top.getValue())).count();
        if (tied > 1) {
            return IntentDecision.unmatched();
        }
        return IntentDecision.matched(top.getKey());
    }

    private <T> ResolveResult<T> resolveEntity(String message, List<T> entities, Function<T, String> nameGetter, String label) {
        if (entities == null || entities.isEmpty()) {
            return ResolveResult.notFound(label + "主数据为空，请先维护" + label + "档案。");
        }
        String normalized = normalizeText(message);
        List<T> hit = entities.stream()
            .filter(Objects::nonNull)
            .filter(entity -> StringUtils.hasText(nameGetter.apply(entity)))
            .filter(entity -> normalized.contains(normalizeText(nameGetter.apply(entity))))
            .toList();
        if (hit.size() == 1) {
            return ResolveResult.resolved(hit.get(0));
        }
        if (hit.size() > 1) {
            String options = hit.stream().map(nameGetter).limit(3).collect(Collectors.joining("、"));
            return ResolveResult.notFound(label + "存在歧义，请明确为：" + options);
        }
        return ResolveResult.notFound("");
    }

    private QuantityParseResult parseQuantity(String message) {
        Matcher matcher = QTY_PATTERN.matcher(message);
        QuantityParseResult fallback = null;
        while (matcher.find()) {
            BigDecimal sourceQty = new BigDecimal(matcher.group(1));
            String unit = matcher.group(2);
            if (!StringUtils.hasText(unit)) {
                if (fallback == null) {
                    fallback = QuantityParseResult.builder()
                        .sourceQty(sourceQty)
                        .qtyKg(sourceQty)
                        .sourceText(sourceQty.stripTrailingZeros().toPlainString() + "kg")
                        .build();
                }
                continue;
            }
            String normalizedUnit = unit.toLowerCase(Locale.ROOT);
            BigDecimal qtyKg = ("斤".equals(normalizedUnit)) ? sourceQty.multiply(HALF) : sourceQty;
            return QuantityParseResult.builder()
                .sourceQty(sourceQty)
                .qtyKg(qtyKg)
                .sourceText(sourceQty.stripTrailingZeros().toPlainString() + unit)
                .build();
        }
        return fallback;
    }

    private BigDecimal parseUnitPrice(String message) {
        Matcher matcher = PRICE_PATTERN.matcher(message);
        if (!matcher.find()) {
            return null;
        }
        return new BigDecimal(matcher.group(1)).setScale(2, RoundingMode.HALF_UP);
    }

    private int resolvePositiveInt(String text, Pattern pattern, int defaultValue, int min, int max) {
        Matcher matcher = pattern.matcher(text == null ? "" : text);
        if (!matcher.find()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(matcher.group(1));
            return Math.max(min, Math.min(max, value));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private ReportRange resolveReportRange(String message) {
        String normalized = normalizeText(message);
        LocalDate today = LocalDate.now();
        if (containsAny(normalized, "本月", "这个月")) {
            LocalDate start = YearMonth.from(today).atDay(1);
            return new ReportRange(start, today, start + " 至 " + today);
        }
        if (containsAny(normalized, "昨天", "昨日")) {
            LocalDate day = today.minusDays(1);
            return new ReportRange(day, day, day.toString());
        }
        int days = resolvePositiveInt(normalized, DAY_PATTERN, 1, 1, 90);
        if (containsAny(normalized, "近", "最近")) {
            LocalDate start = today.minusDays(days - 1L);
            return new ReportRange(start, today, "近" + days + "天");
        }
        LocalDate day = today.minusDays(1);
        return new ReportRange(day, day, day.toString());
    }

    private List<SalesOrder> listCompletedSales(LocalDateTime start, LocalDateTime endExclusive) {
        return salesOrderMapper.selectList(new LambdaQueryWrapper<SalesOrder>()
            .in(SalesOrder::getOrderStatus, List.of("SHIPPED", "CONFIRMED"))
            .ge(SalesOrder::getOrderTime, start)
            .lt(SalesOrder::getOrderTime, endExclusive));
    }

    private String compareConclusion(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) <= 0) {
            if (current.compareTo(BigDecimal.ZERO) <= 0) {
                return "本期与上期均无销售额，建议保持促销活动并观察转化。";
            }
            return "上期销售额为0，本期已产生销售，业务明显回升。";
        }
        BigDecimal ratio = current.subtract(previous).divide(previous, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        int sign = ratio.compareTo(BigDecimal.ZERO);
        if (sign > 0) {
            return "销售额环比上涨 " + ratio.setScale(2, RoundingMode.HALF_UP) + "%，建议继续补货热销品。";
        }
        if (sign < 0) {
            return "销售额环比下降 " + ratio.abs().setScale(2, RoundingMode.HALF_UP) + "%，建议复核库存结构与促销策略。";
        }
        return "销售额与上期基本持平，可维持当前策略并关注毛利变化。";
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object details = authentication.getDetails();
        if (details instanceof Claims claims) {
            Object raw = claims.get("userId");
            if (raw instanceof Number n) {
                return n.longValue();
            }
            if (raw instanceof String s && StringUtils.hasText(s)) {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
    private String normalizeSessionId(String sessionId, Long userId) {
        if (StringUtils.hasText(sessionId)) {
            return sessionId.trim();
        }
        long uid = userId == null ? 0L : userId;
        return "sess-" + uid + "-" + System.currentTimeMillis();
    }

    private Map<Long, String> mapFruitName(Set<Long> fruitIds) {
        if (fruitIds == null || fruitIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return fruitMapper.selectBatchIds(fruitIds).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Fruit::getId, Fruit::getFruitName, (a, b) -> a));
    }

    private Map<Long, String> mapWarehouseName(Set<Long> warehouseIds) {
        if (warehouseIds == null || warehouseIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return warehouseMapper.selectBatchIds(warehouseIds).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Warehouse::getId, Warehouse::getWarehouseName, (a, b) -> a));
    }

    private int hitScore(String text, String... keywords) {
        int score = 0;
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword) && text.contains(keyword.toLowerCase(Locale.ROOT))) {
                score += 2;
            }
        }
        return score;
    }

    private boolean containsAny(String text, String... keywords) {
        if (!StringUtils.hasText(text)) {
            return false;
        }
        for (String keyword : keywords) {
            if (StringUtils.hasText(keyword) && text.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeText(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT)
            .replace("，", " ")
            .replace(",", " ")
            .replace("。", " ")
            .replace(".", " ")
            .replace("！", " ")
            .replace("!", " ")
            .replace("？", " ")
            .replace("?", " ")
            .trim();
    }

    private BigDecimal castBigDecimal(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof BigDecimal decimal) {
            return decimal;
        }
        if (raw instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        if (raw instanceof String s && StringUtils.hasText(s)) {
            return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private Long castLong(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number n) {
            return n.longValue();
        }
        if (raw instanceof String s && StringUtils.hasText(s)) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String safeText(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    @Data
    @Builder
    private static class IntentDecision {
        private boolean matched;
        private String intentCode;

        private static IntentDecision matched(String intentCode) {
            return IntentDecision.builder().matched(true).intentCode(intentCode).build();
        }

        private static IntentDecision unmatched() {
            return IntentDecision.builder().matched(false).build();
        }
    }

    @Data
    @Builder
    private static class QuantityParseResult {
        private BigDecimal sourceQty;
        private BigDecimal qtyKg;
        private String sourceText;
    }

    @Data
    private static class ResolveResult<T> {
        private T entity;
        private String clarification;

        private static <T> ResolveResult<T> resolved(T entity) {
            ResolveResult<T> result = new ResolveResult<>();
            result.setEntity(entity);
            return result;
        }

        private static <T> ResolveResult<T> notFound(String clarification) {
            ResolveResult<T> result = new ResolveResult<>();
            result.setClarification(clarification);
            return result;
        }

        private boolean isResolved() {
            return entity != null;
        }
    }

    @Data
    @Builder
    private static class ReportRange {
        private LocalDate startDate;
        private LocalDate endDate;
        private String label;
    }
}
