package com.fruit.warehouse.module.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiAssistantConfirmResponse {
    private Boolean success;
    private String answer;
    private String bizType;
    private Long bizId;
}

