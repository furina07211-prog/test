package com.fruit.warehouse.module.ai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AiAssistantDispatchResponse {
    private Boolean matched;
    private String intentCode;
    private String answer;
    private Boolean requiresConfirm;
    private String actionId;
    private Map<String, Object> preview;
    private List<String> clarifications;
}

