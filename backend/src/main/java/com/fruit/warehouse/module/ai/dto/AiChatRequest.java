package com.fruit.warehouse.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class AiChatRequest {
    @NotBlank(message = "message cannot be empty")
    private String message;
    private String sessionId;
    private Boolean stream = false;
    private Map<String, Object> context;
}
