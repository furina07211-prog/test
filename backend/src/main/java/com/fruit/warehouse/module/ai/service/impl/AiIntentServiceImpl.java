package com.fruit.warehouse.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.warehouse.module.ai.dto.AiIntentResult;
import com.fruit.warehouse.module.ai.service.AiIntentService;
import com.fruit.warehouse.module.basic.entity.Fruit;
import com.fruit.warehouse.module.basic.mapper.FruitMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiIntentServiceImpl implements AiIntentService {

    private static final String INTENT_INVENTORY_BY_FRUIT = "INVENTORY_BY_FRUIT";
    private static final String INTENT_INVENTORY_OVERVIEW = "INVENTORY_OVERVIEW";
    private static final String INTENT_SALES_REPORT_PERIOD = "SALES_REPORT_PERIOD";
    private static final String INTENT_PURCHASE_SUGGESTION = "PURCHASE_SUGGESTION";

    private static final Pattern DAY_PATTERN = Pattern.compile("(\\d+)\\s*(天|日)", Pattern.CASE_INSENSITIVE);

    private final FruitMapper fruitMapper;

    @Override
    public Optional<AiIntentResult> detect(String message) {
        if (!StringUtils.hasText(message)) {
            return Optional.empty();
        }
        String text = normalize(message);

        if (containsAny(text, "采购建议", "补货建议", "建议采购", "建议补货", "purchase suggestion", "replenishment")) {
            Fruit fruit = resolveFruit(text);
            return Optional.of(AiIntentResult.builder()
                    .intentCode(INTENT_PURCHASE_SUGGESTION)
                    .fruitId(fruit == null ? null : fruit.getId())
                    .fruitName(fruit == null ? null : fruit.getFruitName())
                    .build());
        }

        if (containsAny(text, "销售报表", "销售分析", "销售总结", "report", "报表")) {
            LocalDateTime[] period = resolvePeriod(text);
            return Optional.of(AiIntentResult.builder()
                    .intentCode(INTENT_SALES_REPORT_PERIOD)
                    .startTime(period[0])
                    .endTime(period[1])
                    .build());
        }

        if (containsAny(text, "库存", "在库", "余量", "stock", "inventory")) {
            Fruit fruit = resolveFruit(text);
            if (fruit != null) {
                return Optional.of(AiIntentResult.builder()
                        .intentCode(INTENT_INVENTORY_BY_FRUIT)
                        .fruitId(fruit.getId())
                        .fruitName(fruit.getFruitName())
                        .build());
            }
            return Optional.of(AiIntentResult.builder().intentCode(INTENT_INVENTORY_OVERVIEW).build());
        }

        return Optional.empty();
    }

    private Fruit resolveFruit(String normalizedText) {
        List<Fruit> fruits = fruitMapper.selectList(new LambdaQueryWrapper<Fruit>()
                .select(Fruit::getId, Fruit::getFruitName)
                .eq(Fruit::getStatus, 1));
        for (Fruit fruit : fruits) {
            if (fruit.getFruitName() == null) {
                continue;
            }
            if (normalizedText.contains(normalize(fruit.getFruitName()))) {
                return fruit;
            }
        }
        return null;
    }

    private LocalDateTime[] resolvePeriod(String text) {
        LocalDate today = LocalDate.now();
        if (containsAny(text, "昨天", "昨日", "yesterday")) {
            return new LocalDateTime[] {today.minusDays(1).atStartOfDay(), today.atStartOfDay()};
        }

        Matcher dayMatcher = DAY_PATTERN.matcher(text);
        if ((containsAny(text, "近", "最近") || text.contains("last")) && dayMatcher.find()) {
            int days = Integer.parseInt(dayMatcher.group(1));
            days = Math.max(1, Math.min(days, 90));
            LocalDate start = today.minusDays(days - 1L);
            return new LocalDateTime[] {start.atStartOfDay(), today.plusDays(1).atStartOfDay()};
        }

        return new LocalDateTime[] {today.minusDays(1).atStartOfDay(), today.atStartOfDay()};
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(normalize(keyword))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }
}
