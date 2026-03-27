package com.fruit.warehouse.module.ai.controller;

import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.common.result.Results;
import com.fruit.warehouse.module.ai.dto.AiAssistantConfirmRequest;
import com.fruit.warehouse.module.ai.dto.AiAssistantConfirmResponse;
import com.fruit.warehouse.module.ai.dto.AiAssistantDispatchRequest;
import com.fruit.warehouse.module.ai.dto.AiAssistantDispatchResponse;
import com.fruit.warehouse.module.ai.dto.AiChatRequest;
import com.fruit.warehouse.module.ai.dto.AiChatResponse;
import com.fruit.warehouse.module.ai.dto.AiIntentQueryRequest;
import com.fruit.warehouse.module.ai.dto.AiIntentResult;
import com.fruit.warehouse.module.ai.service.AiAssistantService;
import com.fruit.warehouse.module.ai.service.AiIntentService;
import com.fruit.warehouse.module.ai.service.AiService;
import com.fruit.warehouse.module.ai.service.AiToolQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final AiAssistantService aiAssistantService;

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

    @PostMapping("/assistant/dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<AiAssistantDispatchResponse> assistantDispatch(@Valid @RequestBody AiAssistantDispatchRequest request) {
        return Results.ok(aiAssistantService.dispatch(request));
    }

    @PostMapping("/assistant/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<AiAssistantConfirmResponse> assistantConfirm(@Valid @RequestBody AiAssistantConfirmRequest request) {
        return Results.ok(aiAssistantService.confirm(request));
    }

    @GetMapping("/assistant/history")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<?> assistantHistory(@RequestParam String sessionId,
                                      @RequestParam(defaultValue = "1") Integer pageNo,
                                      @RequestParam(defaultValue = "50") Integer pageSize) {
        return Results.page(aiAssistantService.history(sessionId, pageNo, pageSize));
    }
}

