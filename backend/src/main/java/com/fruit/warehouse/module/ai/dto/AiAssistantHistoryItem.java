package com.fruit.warehouse.module.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiAssistantHistoryItem {
    private String role;
    private String content;
    private LocalDateTime createTime;
    private String intentCode;
    private String toolName;
}

