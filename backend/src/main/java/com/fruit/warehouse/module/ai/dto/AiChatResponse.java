package com.fruit.warehouse.module.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AiChatResponse {
    private String answer;
    private String provider;
    private String model;
    private Boolean fallback;
    private Map<String, Object> usage;
}
