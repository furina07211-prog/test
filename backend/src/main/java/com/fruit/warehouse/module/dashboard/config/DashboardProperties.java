package com.fruit.warehouse.module.dashboard.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "fruit.algorithm")
public class DashboardProperties {
    private String pythonCommand = "python";
    private String scriptPath = "../ai-algorithm/scripts/predict_cli.py";
    private String testDataSqlPath = "../sql/_seed_ascii.sql";
    private Integer timeoutSeconds = 120;
    private Integer defaultSafetyDays = 3;
    private boolean autoEnabled = false;
    private String autoCron = "0 30 2 * * ?";
}
