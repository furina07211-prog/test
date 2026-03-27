package com.fruit.warehouse.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruit.warehouse.module.ai.dto.AiIntentResult;
import com.fruit.warehouse.module.ai.service.AiIntentService;
import com.fruit.warehouse.module.basic.entity.Fruit;
import com.fruit.warehouse.module.basic.mapper.FruitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiIntentServiceImpl implements AiIntentService {

    private static final String INTENT_INVENTORY_BY_FRUIT = "INVENTORY_BY_FRUIT";
    private static final String INTENT_SALES_REPORT_YESTERDAY = "SALES_REPORT_YESTERDAY";

    private final FruitMapper fruitMapper;

    @Override
    public Optional<AiIntentResult> detect(String message) {
        if (message == null || message.isBlank()) {
            return Optional.empty();
        }
        String text = message.trim().toLowerCase();

        if (containsAny(text, "昨天", "昨日", "yesterday") && containsAny(text, "销售", "报表", "日报", "sales", "report")) {
            LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
            LocalDateTime end = LocalDate.now().atStartOfDay();
            return Optional.of(AiIntentResult.builder()
                .intentCode(INTENT_SALES_REPORT_YESTERDAY)
                .startTime(start)
                .endTime(end)
                .build());
        }

        if (containsAny(text, "库存", "存量", "余量", "inventory", "stock")) {
            List<Fruit> fruits = fruitMapper.selectList(new LambdaQueryWrapper<Fruit>()
                .select(Fruit::getId, Fruit::getFruitName)
                .eq(Fruit::getStatus, 1));
            for (Fruit fruit : fruits) {
                if (fruit.getFruitName() != null && text.contains(fruit.getFruitName().toLowerCase())) {
                    return Optional.of(AiIntentResult.builder()
                        .intentCode(INTENT_INVENTORY_BY_FRUIT)
                        .fruitId(fruit.getId())
                        .fruitName(fruit.getFruitName())
                        .build());
                }
            }
        }

        return Optional.empty();
    }

    private boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }
}