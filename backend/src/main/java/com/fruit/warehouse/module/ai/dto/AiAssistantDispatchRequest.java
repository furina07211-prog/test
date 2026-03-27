package com.fruit.warehouse.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiAssistantDispatchRequest {
    @NotBlank(message = "message cannot be empty")
    private String message;
    private String sessionId;
}

