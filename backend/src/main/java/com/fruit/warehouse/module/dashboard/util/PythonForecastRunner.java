package com.fruit.warehouse.module.dashboard.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.dashboard.config.DashboardProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PythonForecastRunner {

    private final DashboardProperties properties;
    private final ObjectMapper objectMapper;

    public JsonNode runForecast(Map<String, Object> payload) {
        return execute("forecast", payload);
    }

    public JsonNode runOptimize(Map<String, Object> payload) {
        return execute("optimize", payload);
    }

    private JsonNode execute(String mode, Map<String, Object> payload) {
        Path script = Paths.get(properties.getScriptPath()).normalize();
        ProcessBuilder builder = new ProcessBuilder(properties.getPythonCommand(), script.toString(), mode);
        builder.redirectErrorStream(false);

        try {
            Process process = builder.start();
            String input = objectMapper.writeValueAsString(payload);
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
                writer.write(input);
                writer.flush();
            }

            boolean finished = process.waitFor(Duration.ofSeconds(properties.getTimeoutSeconds()).toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException("Python脚本执行超时");
            }

            String stdout = readAll(process.getInputStream());
            String stderr = readAll(process.getErrorStream());
            if (process.exitValue() != 0) {
                throw new BusinessException("Python脚本执行失败：" + stderr);
            }
            if (stdout == null || stdout.isBlank()) {
                throw new BusinessException("Python脚本返回为空");
            }
            return objectMapper.readTree(stdout);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Python执行异常：" + ex.getMessage());
        }
    }

    private String readAll(java.io.InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}