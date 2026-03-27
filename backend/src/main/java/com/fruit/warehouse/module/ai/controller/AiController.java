package com.fruit.warehouse.module.ai.controller;

import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.common.result.Results;
import com.fruit.warehouse.module.ai.dto.AiChatRequest;
import com.fruit.warehouse.module.ai.dto.AiChatResponse;
import com.fruit.warehouse.module.ai.dto.AiIntentQueryRequest;
import com.fruit.warehouse.module.ai.dto.AiIntentResult;
import com.fruit.warehouse.module.ai.service.AiIntentService;
import com.fruit.warehouse.module.ai.service.AiService;
import com.fruit.warehouse.module.ai.service.AiToolQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiIntentService aiIntentService;
    private final AiToolQueryService aiToolQueryService;

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        request.setStream(false);
        return Results.ok(aiService.chat(request));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public SseEmitter chatStream(@Valid @RequestBody AiChatRequest request) {
        request.setStream(true);
        SseEmitter emitter = new SseEmitter(0L);
        aiService.chatStream(request, emitter);
        return emitter;
    }

    @PostMapping("/intent/query")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<Map<String, Object>> intentQuery(@Valid @RequestBody AiIntentQueryRequest request) {
        Optional<AiIntentResult> intent = aiIntentService.detect(request.getMessage());
        if (intent.isEmpty()) {
            return Results.ok(Map.of(
                "matched", false,
                "answer", "未识别到白名单业务意图，请改用自然语言对话。"
            ));
        }

        String answer = aiToolQueryService.execute(intent.get());
        return Results.ok(Map.of(
            "matched", true,
            "intentCode", intent.get().getIntentCode(),
            "answer", answer
        ));
    }
}
