package com.fruit.warehouse.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.basic.entity.Fruit;
import com.fruit.warehouse.module.basic.mapper.FruitMapper;
import com.fruit.warehouse.module.dashboard.config.DashboardProperties;
import com.fruit.warehouse.module.dashboard.dto.DailyPoint;
import com.fruit.warehouse.module.dashboard.dto.ForecastRunRequest;
import com.fruit.warehouse.module.dashboard.dto.ForecastTrendResponse;
import com.fruit.warehouse.module.dashboard.dto.HeatmapPoint;
import com.fruit.warehouse.module.dashboard.dto.OptimizeRunRequest;
import com.fruit.warehouse.module.dashboard.dto.StockSummaryPoint;
import com.fruit.warehouse.module.dashboard.entity.AiForecastResult;
import com.fruit.warehouse.module.dashboard.entity.AiPurchaseSuggestion;
import com.fruit.warehouse.module.dashboard.mapper.AiForecastResultMapper;
import com.fruit.warehouse.module.dashboard.mapper.AiPurchaseSuggestionMapper;
import com.fruit.warehouse.module.dashboard.mapper.DashboardQueryMapper;
import com.fruit.warehouse.module.dashboard.service.DashboardService;
import com.fruit.warehouse.module.dashboard.util.PythonForecastRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final Pattern JDBC_PATTERN = Pattern.compile("jdbc:mysql://([^:/?#]+)(?::(\\d+))?/([^?;]+).*");

    private final DashboardQueryMapper dashboardQueryMapper;
    private final AiForecastResultMapper forecastResultMapper;
    private final AiPurchaseSuggestionMapper purchaseSuggestionMapper;
    private final FruitMapper fruitMapper;
    private final PythonForecastRunner pythonForecastRunner;
    private final DashboardProperties dashboardProperties;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;
    @Value("${spring.datasource.username:}")
    private String datasourceUsername;
    @Value("${spring.datasource.password:}")
    private String datasourcePassword;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> runForecast(ForecastRunRequest request) {
        List<Long> fruitIds = resolveFruitIds(request.getFruitId());
        int savedCount = 0;

        for (Long fruitId : fruitIds) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("fruitId", fruitId);
            payload.put("days", request.getDays() == null ? 7 : request.getDays());
            payload.put("model", request.getModel() == null ? "prophet" : request.getModel());
            payload.put("db", buildDbConfig());
            payload.put("testDataSqlPath", dashboardProperties.getTestDataSqlPath());

            JsonNode result = pythonForecastRunner.runForecast(payload);
            JsonNode predictions = result.path("predictions");
            if (!predictions.isArray()) {
                continue;
            }
            int dataWindow = result.path("history").isArray() ? result.path("history").size() : 0;
            for (JsonNode node : predictions) {
                upsertForecast(fruitId, payload.get("model").toString(), node, dataWindow);
                savedCount++;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("processedFruits", fruitIds.size());
        response.put("savedForecastRows", savedCount);
        return response;
    }

    @Override
    public ForecastTrendResponse getForecastTrend(Long fruitId, Integer historyDays) {
        if (fruitId == null) {
            throw new BusinessException("fruitId is required");
        }
        LocalDate startDate = LocalDate.now().minusDays(historyDays == null ? 30 : historyDays);
        List<DailyPoint> history = dashboardQueryMapper.selectDailySales(fruitId, startDate);

        List<AiForecastResult> all = forecastResultMapper.selectList(new LambdaQueryWrapper<AiForecastResult>()
            .eq(AiForecastResult::getFruitId, fruitId)
            .orderByDesc(AiForecastResult::getForecastDate)
            .orderByAsc(AiForecastResult::getTargetDate));

        List<AiForecastResult> latest = new ArrayList<>();
        if (!all.isEmpty()) {
            LocalDate latestDate = all.get(0).getForecastDate();
            for (AiForecastResult row : all) {
                if (latestDate.equals(row.getForecastDate())) {
                    latest.add(row);
                }
            }
        }

        Fruit fruit = fruitMapper.selectById(fruitId);
        ForecastTrendResponse response = new ForecastTrendResponse();
        response.setFruitId(fruitId);
        response.setFruitName(fruit == null ? "" : fruit.getFruitName());
        response.setHistory(history);
        response.setForecast(latest);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AiPurchaseSuggestion> runOptimize(OptimizeRunRequest request) {
        Long warehouseId = request.getWarehouseId() == null ? 1L : request.getWarehouseId();
        Integer leadTimeDays = request.getLeadTimeDays() == null ? 1 : request.getLeadTimeDays();
        Integer safetyDays = request.getSafetyDays() == null ? dashboardProperties.getDefaultSafetyDays() : request.getSafetyDays();

        List<StockSummaryPoint> stockRows = dashboardQueryMapper.selectCurrentStock(request.getFruitId(), warehouseId);
        List<StockSummaryPoint> inTransitRows = dashboardQueryMapper.selectInTransit(request.getFruitId(), warehouseId);
        Map<String, BigDecimal> inTransitMap = new HashMap<>();
        for (StockSummaryPoint row : inTransitRows) {
            inTransitMap.put(key(row.getFruitId(), row.getWarehouseId()), row.getInTransitQty() == null ? BigDecimal.ZERO : row.getInTransitQty());
        }

        List<AiPurchaseSuggestion> saved = new ArrayList<>();
        for (StockSummaryPoint row : stockRows) {
            BigDecimal predictedDailyQty = calcPredictedDailyQty(row.getFruitId());
            BigDecimal safetyStockQty = predictedDailyQty.multiply(BigDecimal.valueOf(safetyDays));
            BigDecimal currentStockQty = row.getCurrentStockQty() == null ? BigDecimal.ZERO : row.getCurrentStockQty();
            BigDecimal inTransitQty = inTransitMap.getOrDefault(key(row.getFruitId(), warehouseId), BigDecimal.ZERO);

            Map<String, Object> payload = new HashMap<>();
            payload.put("predictedDailyQty", predictedDailyQty);
            payload.put("leadTimeDays", leadTimeDays);
            payload.put("safeStockQty", safetyStockQty);
            payload.put("currentStockQty", currentStockQty);
            payload.put("inTransitQty", inTransitQty);
            JsonNode optimize = pythonForecastRunner.runOptimize(payload);

            AiPurchaseSuggestion suggestion = upsertSuggestion(row, warehouseId, leadTimeDays, optimize);
            saved.add(suggestion);
        }

        return saved;
    }

    @Override
    public List<AiPurchaseSuggestion> listOptimize(Long fruitId, Long warehouseId, Integer limit) {
        int actualLimit = limit == null ? 20 : limit;
        LambdaQueryWrapper<AiPurchaseSuggestion> wrapper = new LambdaQueryWrapper<AiPurchaseSuggestion>()
            .eq(fruitId != null, AiPurchaseSuggestion::getFruitId, fruitId)
            .eq(warehouseId != null, AiPurchaseSuggestion::getWarehouseId, warehouseId)
            .orderByDesc(AiPurchaseSuggestion::getSuggestionDate)
            .orderByDesc(AiPurchaseSuggestion::getCreatedTime)
            .last("limit " + actualLimit);
        return purchaseSuggestionMapper.selectList(wrapper);
    }

    @Override
    public List<HeatmapPoint> getAlertHeatmap() {
        return dashboardQueryMapper.selectAlertHeatmap();
    }

    private List<Long> resolveFruitIds(Long fruitId) {
        if (fruitId != null) {
            return List.of(fruitId);
        }
        List<Fruit> fruits = fruitMapper.selectList(new LambdaQueryWrapper<Fruit>()
            .select(Fruit::getId)
            .eq(Fruit::getStatus, 1));
        List<Long> ids = new ArrayList<>();
        for (Fruit fruit : fruits) {
            ids.add(fruit.getId());
        }
        return ids;
    }

    private void upsertForecast(Long fruitId, String model, JsonNode node, int dataWindowDays) {
        LocalDate targetDate = LocalDate.parse(node.path("targetDate").asText());
        BigDecimal predictQty = node.path("predictQty").decimalValue();
        BigDecimal lower = node.path("confidenceLower").decimalValue();
        BigDecimal upper = node.path("confidenceUpper").decimalValue();

        LambdaQueryWrapper<AiForecastResult> wrapper = new LambdaQueryWrapper<AiForecastResult>()
            .eq(AiForecastResult::getFruitId, fruitId)
            .eq(AiForecastResult::getTargetDate, targetDate)
            .eq(AiForecastResult::getModelName, model)
            .eq(AiForecastResult::getVersionNo, "v1");
        AiForecastResult exists = forecastResultMapper.selectOne(wrapper);

        if (exists == null) {
            exists = new AiForecastResult();
            exists.setFruitId(fruitId);
            exists.setTargetDate(targetDate);
            exists.setModelName(model);
            exists.setVersionNo("v1");
            exists.setCreatedTime(LocalDateTime.now());
            exists.setForecastDate(LocalDate.now());
            exists.setPredictQty(predictQty);
            exists.setConfidenceLower(lower);
            exists.setConfidenceUpper(upper);
            exists.setDataWindowDays(dataWindowDays);
            forecastResultMapper.insert(exists);
        } else {
            exists.setForecastDate(LocalDate.now());
            exists.setPredictQty(predictQty);
            exists.setConfidenceLower(lower);
            exists.setConfidenceUpper(upper);
            exists.setDataWindowDays(dataWindowDays);
            forecastResultMapper.updateById(exists);
        }
    }

    private BigDecimal calcPredictedDailyQty(Long fruitId) {
        LocalDate today = LocalDate.now();
        List<AiForecastResult> forecasts = forecastResultMapper.selectList(new LambdaQueryWrapper<AiForecastResult>()
            .eq(AiForecastResult::getFruitId, fruitId)
            .ge(AiForecastResult::getTargetDate, today)
            .orderByDesc(AiForecastResult::getForecastDate)
            .orderByAsc(AiForecastResult::getTargetDate)
            .last("limit 7"));

        if (forecasts.isEmpty()) {
            List<DailyPoint> history = dashboardQueryMapper.selectDailySales(fruitId, today.minusDays(7));
            if (history.isEmpty()) {
                return BigDecimal.ZERO;
            }
            BigDecimal sum = BigDecimal.ZERO;
            for (DailyPoint point : history) {
                sum = sum.add(point.getQty() == null ? BigDecimal.ZERO : point.getQty());
            }
            return sum.divide(BigDecimal.valueOf(history.size()), 2, java.math.RoundingMode.HALF_UP);
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (AiForecastResult row : forecasts) {
            sum = sum.add(row.getPredictQty() == null ? BigDecimal.ZERO : row.getPredictQty());
        }
        return sum.divide(BigDecimal.valueOf(forecasts.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    private AiPurchaseSuggestion upsertSuggestion(StockSummaryPoint row, Long warehouseId, Integer leadTimeDays, JsonNode optimize) {
        LocalDate suggestionDate = LocalDate.now();
        LambdaQueryWrapper<AiPurchaseSuggestion> wrapper = new LambdaQueryWrapper<AiPurchaseSuggestion>()
            .eq(AiPurchaseSuggestion::getFruitId, row.getFruitId())
            .eq(AiPurchaseSuggestion::getWarehouseId, warehouseId)
            .eq(AiPurchaseSuggestion::getSuggestionDate, suggestionDate);
        AiPurchaseSuggestion exists = purchaseSuggestionMapper.selectOne(wrapper);

        if (exists == null) {
            exists = new AiPurchaseSuggestion();
            exists.setFruitId(row.getFruitId());
            exists.setWarehouseId(warehouseId);
            exists.setSuggestionDate(suggestionDate);
            exists.setCreatedTime(LocalDateTime.now());
            exists.setStatus("NEW");
        }

        exists.setPredictedDailyQty(optimize.path("predictedDailyQty").decimalValue());
        exists.setLeadTimeDays(leadTimeDays);
        exists.setSafetyStockQty(optimize.path("safetyStockQty").decimalValue());
        exists.setCurrentStockQty(optimize.path("currentStockQty").decimalValue());
        exists.setInTransitQty(optimize.path("inTransitQty").decimalValue());
        exists.setRecommendedPurchaseQty(optimize.path("recommendedPurchaseQty").decimalValue());
        exists.setReason("Auto generated by forecast + inventory optimization");

        if (exists.getId() == null) {
            purchaseSuggestionMapper.insert(exists);
        } else {
            purchaseSuggestionMapper.updateById(exists);
        }

        return exists;
    }

    private Map<String, Object> buildDbConfig() {
        Matcher matcher = JDBC_PATTERN.matcher(datasourceUrl == null ? "" : datasourceUrl);
        Map<String, Object> db = new HashMap<>();
        if (matcher.matches()) {
            db.put("host", matcher.group(1));
            db.put("port", Integer.parseInt(matcher.group(2) == null ? "3306" : matcher.group(2)));
            db.put("database", matcher.group(3));
            db.put("user", datasourceUsername);
            db.put("password", datasourcePassword);
            db.put("charset", "utf8mb4");
        }
        return db;
    }

    private String key(Long fruitId, Long warehouseId) {
        return fruitId + "#" + warehouseId;
    }
}