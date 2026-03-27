package com.fruit.warehouse.module.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fruit.ai")
public class AiProperties {
    private boolean enabled = true;
    private String provider = "openai-compatible";
    private String baseUrl = "https://api.openai.com";
    private String apiKey;
    private String model = "gpt-4o-mini";
    private Integer timeoutMs = 30000;
    private Double temperature = 0.3;
    private Integer maxTokens = 1024;
}
