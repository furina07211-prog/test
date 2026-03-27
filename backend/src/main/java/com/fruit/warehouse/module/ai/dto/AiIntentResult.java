package com.fruit.warehouse.module.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AiIntentResult {
    private String intentCode;
    private Long fruitId;
    private String fruitName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
