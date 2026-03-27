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

/**
 * AI智能助手 模块控制器。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiIntentService aiIntentService;
    private final AiToolQueryService aiToolQueryService;
    private final AiAssistantService aiAssistantService;

    /**
     * 非流式对话：用于普通问答与业务文本回复。
     */
    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        request.setStream(false);
        return Results.ok(aiService.chat(request));
    }

    /**
     * 流式对话：以 SSE 持续推送模型输出。
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public SseEmitter chatStream(@Valid @RequestBody AiChatRequest request) {
        request.setStream(true);
        SseEmitter emitter = new SseEmitter(0L);
        try {
            aiService.chatStream(request, emitter);
        } catch (Exception e) {
            try {
                emitter.send(SseEmitter.event().name("error").data("AI对话处理失败：" + e.getMessage()));
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            } catch (Exception ignored) {
                // ignore send failure
            }
            emitter.complete();
        }
        return emitter;
    }

    /**
     * 意图识别查询：命中业务意图则执行工具查询，否则提示回退自由对话。
     */
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

    /**
     * 智能助手分发入口：负责识别、澄清与预览结果组装。
     */
    @PostMapping("/assistant/dispatch")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<AiAssistantDispatchResponse> assistantDispatch(@Valid @RequestBody AiAssistantDispatchRequest request) {
        return Results.ok(aiAssistantService.dispatch(request));
    }

    /**
     * 智能助手确认入口：对写操作执行二次确认/取消。
     */
    @PostMapping("/assistant/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<AiAssistantConfirmResponse> assistantConfirm(@Valid @RequestBody AiAssistantConfirmRequest request) {
        return Results.ok(aiAssistantService.confirm(request));
    }

    /**
     * 按会话分页查询对话历史。
     */
    @GetMapping("/assistant/history")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<?> assistantHistory(@RequestParam String sessionId,
                                      @RequestParam(defaultValue = "1") Integer pageNo,
                                      @RequestParam(defaultValue = "50") Integer pageSize) {
        return Results.page(aiAssistantService.history(sessionId, pageNo, pageSize));
    }
}

