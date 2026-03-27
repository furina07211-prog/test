package com.fruit.warehouse.module.ai.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruit.warehouse.common.exception.BusinessException;
import com.fruit.warehouse.module.ai.config.AiProperties;
import com.fruit.warehouse.module.ai.dto.AiChatRequest;
import com.fruit.warehouse.module.ai.dto.AiChatResponse;
import com.fruit.warehouse.module.ai.dto.AiIntentResult;
import com.fruit.warehouse.module.ai.service.AiIntentService;
import com.fruit.warehouse.module.ai.service.AiService;
import com.fruit.warehouse.module.ai.service.AiToolQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private static final String SYSTEM_PROMPT = "You are a warehouse assistant. Give concise and practical answers in Chinese.";

    private final AiProperties aiProperties;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final AiIntentService aiIntentService;
    private final AiToolQueryService aiToolQueryService;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        Optional<AiIntentResult> intent = aiIntentService.detect(request.getMessage());
        if (intent.isPresent()) {
            String answer = aiToolQueryService.execute(intent.get());
            return AiChatResponse.builder()
                .answer(answer)
                .provider("tool")
                .model("intent-router")
                .fallback(true)
                .usage(Map.of("source", "intent"))
                .build();
        }

        validateAiConfig();

        Map<String, Object> body = buildChatBody(request, false);
        try {
            String response = buildClient()
                .post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(aiProperties.getTimeoutMs()))
                .block();

            return parseNonStreamResponse(response);
        } catch (WebClientResponseException e) {
            throw new BusinessException("AI service error: " + extractWebClientMessage(e));
        } catch (Exception e) {
            throw new BusinessException("AI request failed: " + e.getMessage());
        }
    }

    @Override
    public void chatStream(AiChatRequest request, SseEmitter emitter) {
        Optional<AiIntentResult> intent = aiIntentService.detect(request.getMessage());
        if (intent.isPresent()) {
            try {
                emitter.send(SseEmitter.event().name("chunk").data(aiToolQueryService.execute(intent.get())));
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
                return;
            } catch (IOException e) {
                emitter.completeWithError(e);
                return;
            }
        }

        validateAiConfig();

        Map<String, Object> body = buildChatBody(request, true);
        StringBuilder lineBuffer = new StringBuilder();

        Disposable disposable = buildClient()
            .post()
            .uri("/v1/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(String.class)
            .timeout(Duration.ofMillis(aiProperties.getTimeoutMs()))
            .subscribe(
                chunk -> parseStreamChunk(chunk, lineBuffer, emitter),
                error -> completeWithError(emitter, "AI stream failed: " + error.getMessage()),
                () -> {
                    flushRemaining(lineBuffer, emitter);
                    safeSendDone(emitter);
                    emitter.complete();
                }
            );

        emitter.onTimeout(() -> {
            disposable.dispose();
            completeWithError(emitter, "AI stream timeout");
        });
        emitter.onCompletion(disposable::dispose);
        emitter.onError(ex -> disposable.dispose());
    }

    private void parseStreamChunk(String chunk, StringBuilder lineBuffer, SseEmitter emitter) {
        lineBuffer.append(chunk);
        int idx;
        while ((idx = lineBuffer.indexOf("\n")) >= 0) {
            String line = lineBuffer.substring(0, idx).trim();
            lineBuffer.delete(0, idx + 1);
            handleSseLine(line, emitter);
        }
    }

    private void flushRemaining(StringBuilder lineBuffer, SseEmitter emitter) {
        String remain = lineBuffer.toString().trim();
        if (!remain.isEmpty()) {
            handleSseLine(remain, emitter);
        }
        lineBuffer.setLength(0);
    }

    private void handleSseLine(String line, SseEmitter emitter) {
        if (!line.startsWith("data:")) {
            return;
        }
        String payload = line.substring(5).trim();
        if (payload.isEmpty()) {
            return;
        }
        if ("[DONE]".equals(payload)) {
            safeSendDone(emitter);
            return;
        }

        try {
            JsonNode node = objectMapper.readTree(payload);
            JsonNode delta = node.path("choices").path(0).path("delta").path("content");
            if (delta.isTextual() && StringUtils.hasText(delta.asText())) {
                emitter.send(SseEmitter.event().name("chunk").data(delta.asText()));
            }
        } catch (Exception e) {
            completeWithError(emitter, "AI stream parse error: " + e.getMessage());
        }
    }

    private AiChatResponse parseNonStreamResponse(String response) {
        if (!StringUtils.hasText(response)) {
            throw new BusinessException("AI service returned empty response");
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            String answer = root.path("choices").path(0).path("message").path("content").asText();
            Map<String, Object> usage = new HashMap<>();
            JsonNode usageNode = root.path("usage");
            if (usageNode != null && !usageNode.isMissingNode()) {
                usage = objectMapper.convertValue(usageNode, Map.class);
            }
            if (!StringUtils.hasText(answer)) {
                throw new BusinessException("AI response did not include answer text");
            }
            return AiChatResponse.builder()
                .answer(answer)
                .provider(aiProperties.getProvider())
                .model(aiProperties.getModel())
                .fallback(false)
                .usage(usage)
                .build();
        } catch (JsonProcessingException e) {
            throw new BusinessException("Failed to parse AI response: " + e.getMessage());
        }
    }

    private Map<String, Object> buildChatBody(AiChatRequest request, boolean stream) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        String userMessage = request.getMessage();
        if (request.getContext() != null && !request.getContext().isEmpty()) {
            try {
                String contextJson = objectMapper.writeValueAsString(request.getContext());
                userMessage = userMessage + "\n\n[context]\n" + contextJson;
            } catch (JsonProcessingException ignored) {
                // ignore context serialization failure
            }
        }
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> body = new HashMap<>();
        body.put("model", aiProperties.getModel());
        body.put("messages", messages);
        body.put("temperature", aiProperties.getTemperature());
        body.put("max_tokens", aiProperties.getMaxTokens());
        body.put("stream", stream);
        return body;
    }

    private WebClient buildClient() {
        return webClientBuilder
            .baseUrl(normalizeBaseUrl(aiProperties.getBaseUrl()))
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey())
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "https://api.openai.com";
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    private void validateAiConfig() {
        if (!aiProperties.isEnabled()) {
            throw new BusinessException("AI feature is disabled");
        }
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new BusinessException("AI API key is not configured");
        }
    }

    private String extractWebClientMessage(WebClientResponseException e) {
        try {
            String body = e.getResponseBodyAsString();
            if (!StringUtils.hasText(body)) {
                return e.getStatusCode() + " " + e.getStatusText();
            }
            JsonNode node = objectMapper.readTree(body);
            String msg = node.path("error").path("message").asText();
            return StringUtils.hasText(msg) ? msg : body;
        } catch (Exception ignored) {
            return e.getMessage();
        }
    }

    private void safeSendDone(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
        } catch (IOException ignored) {
            // ignore
        }
    }

    private void completeWithError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
        } catch (IOException ignored) {
            // ignore
        }
        emitter.completeWithError(new BusinessException(message));
    }
}
