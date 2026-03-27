package com.fruit.warehouse.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiAssistantConfirmRequest {
    @NotBlank(message = "actionId cannot be empty")
    private String actionId;
    private Boolean confirm = Boolean.TRUE;
    private String sessionId;
}

